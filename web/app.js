'use strict';

// ═══════════════════════════════════════════════════════
//  Constants
// ═══════════════════════════════════════════════════════
// Conservative browser canvas limits (especially mobile Safari)
const MAX_CANVAS_DIM    = 4096;   // max single dimension px
const MAX_CANVAS_PIXELS = 16e6;   // ~16 megapixel total
const SMART_TOL         = 10;     // colour tolerance for smart split

// ═══════════════════════════════════════════════════════
//  State
// ═══════════════════════════════════════════════════════
const state = {
  mode:      'stitch',
  direction: 'vertical',
  files:     [],
  wmFile:    null,
  results:   []
};

// ═══════════════════════════════════════════════════════
//  DOM refs
// ═══════════════════════════════════════════════════════
const $ = id => document.getElementById(id);

const dom = {
  tabs:          document.querySelectorAll('.tab'),
  dropZone:      $('drop-zone'),
  dropLabel:     $('drop-label'),
  fileInput:     $('file-input'),
  fileList:      $('file-list'),
  wmImportRow:   $('wm-import-row'),
  wmImgBtn:      $('wm-img-btn'),
  wmImgInput:    $('wm-img-input'),
  wmImgLabel:    $('wm-img-label'),

  rowDirection:  $('row-direction'),
  btnVertical:   $('btn-vertical'),
  btnHorizontal: $('btn-horizontal'),
  rowSsCombo:    $('row-ss-combo'),
  ssCombo:       $('ss-combo'),
  rowParts:      $('row-parts'),
  partsInput:    $('parts-input'),
  wmOpts:        $('wm-opts'),
  opacityRange:  $('opacity-range'),
  opacityVal:    $('opacity-val'),
  greyscaleChk:  $('greyscale-chk'),
  wmCount:       $('wm-count'),
  wmWidthPct:    $('wm-width-pct'),
  filename:      $('filename'),
  fmtSelect:     $('fmt-select'),

  runBtn:        $('run-btn'),
  progressWrap:  $('progress-wrap'),
  progressFill:  $('progress-fill'),
  progressLabel: $('progress-label'),

  resultSection: $('result-section'),
  resultInfo:    $('result-info'),
  downloadBtn:   $('download-btn'),
  resultGrid:    $('result-grid'),
};

// ═══════════════════════════════════════════════════════
//  UI helpers
// ═══════════════════════════════════════════════════════
const show   = el => el.classList.remove('hidden');
const hide   = el => el.classList.add('hidden');
const toggle = (el, v) => el.classList.toggle('hidden', !v);

function applyMode(mode) {
  state.mode  = mode;
  state.files = [];
  state.wmFile = null;
  renderFileList();
  hideResult();

  dom.tabs.forEach(t => t.classList.toggle('active', t.dataset.mode === mode));

  toggle(dom.rowDirection, mode === 'stitch' || mode === 'split');
  toggle(dom.rowSsCombo,   mode === 'stitchsplit');
  toggle(dom.rowParts,     mode === 'split' || mode === 'smartsplit' || mode === 'stitchsplit');
  toggle(dom.wmOpts,       mode === 'watermark');
  toggle(dom.wmImportRow,  mode === 'watermark');

  const multi = mode === 'stitch' || mode === 'stitchsplit';
  dom.fileInput.multiple = multi;
  dom.dropLabel.textContent = multi
    ? 'Tap to import images (select multiple)'
    : 'Tap to import image';

  dom.partsInput.placeholder = mode === 'smartsplit' ? 'approx.' : '';
}

// ─── File list ───────────────────────────────────────
function formatBytes(b) {
  if (b < 1024)        return b + ' B';
  if (b < 1024 * 1024) return (b / 1024).toFixed(0) + ' KB';
  return (b / (1024 * 1024)).toFixed(1) + ' MB';
}

function renderFileList() {
  dom.fileList.innerHTML = '';
  state.files.forEach((file, i) => {
    const li   = document.createElement('li');
    li.className = 'file-item';

    const thumb = document.createElement('img');
    thumb.className = 'file-thumb';
    thumb.alt = file.name;
    const url = URL.createObjectURL(file);
    thumb.src = url;
    thumb.onload = () => URL.revokeObjectURL(url);

    const name = document.createElement('span');
    name.className = 'file-name';
    name.textContent = file.name;

    const size = document.createElement('span');
    size.className = 'file-size';
    size.textContent = formatBytes(file.size);

    const rm = document.createElement('button');
    rm.className = 'file-remove';
    rm.title = 'Remove';
    rm.textContent = '×';
    rm.addEventListener('click', () => { state.files.splice(i, 1); renderFileList(); });

    li.append(thumb, name, size, rm);
    dom.fileList.appendChild(li);
  });
}

// ─── Result grid ─────────────────────────────────────
function showResult(canvases) {
  state.results    = canvases;
  dom.resultGrid.innerHTML = '';
  const fmt = dom.fmtSelect.value;
  const ext = fmt === 'jpeg' ? 'jpg' : fmt;

  canvases.forEach((canvas, i) => {
    const wrap = document.createElement('div');
    wrap.className = 'result-thumb-wrap';
    wrap.title = `Click to download image ${i + 1}`;

    const img = document.createElement('img');
    img.className = 'result-thumb';
    img.alt = `Result ${i + 1}`;
    // Use small preview quality for thumbnails
    img.src = canvas.toDataURL(`image/${fmt}`, 0.5);

    const label = document.createElement('div');
    label.className = 'result-thumb-label';
    label.textContent = `${i + 1}.${ext}`;

    wrap.append(img, label);
    wrap.addEventListener('click', () => {
      const base = dom.filename.value.trim() || 'output';
      const name = canvases.length > 1 ? `${base}_${i + 1}` : base;
      downloadSingle(canvas, name, fmt);
    });
    dom.resultGrid.appendChild(wrap);
  });

  const n = canvases.length;
  dom.resultInfo.textContent = n === 1 ? '1 image' : `${n} images`;
  show(dom.resultSection);
  dom.resultSection.scrollIntoView({ behavior: 'smooth', block: 'start' });
}

function hideResult() {
  hide(dom.resultSection);
  dom.resultGrid.innerHTML = '';
  state.results = [];
}

// ─── Progress ─────────────────────────────────────────
function setProgress(pct, label) {
  dom.progressFill.style.width = pct + '%';
  dom.progressLabel.textContent = label || 'Processing…';
}
function showProgress(label) { setProgress(0, label); show(dom.progressWrap); }
function hideProgress()       { hide(dom.progressWrap); }

// ═══════════════════════════════════════════════════════
//  Core utilities
// ═══════════════════════════════════════════════════════
function loadImage(file) {
  return new Promise((resolve, reject) => {
    const url = URL.createObjectURL(file);
    const img = new Image();
    img.onload  = () => { URL.revokeObjectURL(url); resolve(img); };
    img.onerror = () => { URL.revokeObjectURL(url); reject(new Error('Failed to load: ' + file.name)); };
    img.src = url;
  });
}

function canvasToBlob(canvas, format, quality = 0.92) {
  return new Promise(resolve => canvas.toBlob(resolve, `image/${format}`, quality));
}

const yield_ = () => new Promise(r => setTimeout(r, 0));

function imgW(img) { return img.naturalWidth  || img.width;  }
function imgH(img) { return img.naturalHeight || img.height; }

// Verify a canvas isn't blank due to size limits
function canvasFailed(canvas) {
  const ctx = canvas.getContext('2d');
  if (!ctx) return true;
  try {
    // Check a pixel in the middle — if everything is 0 and canvas is large it's a failed alloc
    const d = ctx.getImageData(Math.floor(canvas.width / 2), Math.floor(canvas.height / 2), 1, 1).data;
    return false; // getImageData succeeded, canvas is valid
  } catch { return true; }
}

// Create canvas (returns null if dimensions are too large)
function safeCanvas(w, h) {
  if (w <= 0 || h <= 0) return null;
  const c = document.createElement('canvas');
  c.width  = w;
  c.height = h;
  // If canvas silently failed (iOS/Android limit), getContext may return null
  if (!c.getContext('2d')) return null;
  return c;
}

// ═══════════════════════════════════════════════════════
//  Scale-to-fit helper
//  Reduces image dimensions so the combined canvas fits limits.
// ═══════════════════════════════════════════════════════
function computeScale(imgs, direction) {
  let totalW, totalH;
  if (direction === 'vertical') {
    totalW = Math.max(...imgs.map(imgW));
    totalH = imgs.reduce((s, i) => s + imgH(i), 0);
  } else {
    totalW = imgs.reduce((s, i) => s + imgW(i), 0);
    totalH = Math.max(...imgs.map(imgH));
  }

  let scale = 1;
  if (totalW > MAX_CANVAS_DIM) scale = Math.min(scale, MAX_CANVAS_DIM / totalW);
  if (totalH > MAX_CANVAS_DIM) scale = Math.min(scale, MAX_CANVAS_DIM / totalH);
  if (totalW * totalH * scale * scale > MAX_CANVAS_PIXELS)
    scale = Math.min(scale, Math.sqrt(MAX_CANVAS_PIXELS / (totalW * totalH)));

  return { scale, totalW, totalH };
}

// ═══════════════════════════════════════════════════════
//  STITCH — creates one canvas (scales if needed)
// ═══════════════════════════════════════════════════════
function stitchImages(imgs, direction, scale = 1) {
  const sw = direction === 'vertical'
    ? Math.max(...imgs.map(imgW))
    : imgs.reduce((s, i) => s + imgW(i), 0);
  const sh = direction === 'vertical'
    ? imgs.reduce((s, i) => s + imgH(i), 0)
    : Math.max(...imgs.map(imgH));

  const cw = Math.round(sw * scale);
  const ch = Math.round(sh * scale);

  const canvas = safeCanvas(cw, ch);
  if (!canvas) throw new Error(`Canvas too large (${cw}×${ch}). Try fewer/smaller images.`);

  const ctx = canvas.getContext('2d');

  if (direction === 'vertical') {
    let y = 0;
    for (const img of imgs) {
      const dh = Math.round(imgH(img) * scale);
      const dw = Math.round(imgW(img) * scale);
      ctx.drawImage(img, 0, y, dw, dh);
      y += dh;
    }
  } else {
    let x = 0;
    for (const img of imgs) {
      const dw = Math.round(imgW(img) * scale);
      const dh = Math.round(imgH(img) * scale);
      ctx.drawImage(img, x, 0, dw, dh);
      x += dw;
    }
  }
  return canvas;
}

// ═══════════════════════════════════════════════════════
//  SPLIT — equal parts from an existing canvas
// ═══════════════════════════════════════════════════════
function splitCanvas(canvas, numParts, direction) {
  const results = [];
  if (direction === 'horizontal') {
    const partH = Math.floor(canvas.height / numParts);
    const rem   = canvas.height % numParts;
    for (let i = 0; i < numParts; i++) {
      const y = i * partH;
      const h = partH + (i === numParts - 1 ? rem : 0);
      const c = safeCanvas(canvas.width, h);
      if (!c) throw new Error('Split part too large for canvas.');
      c.getContext('2d').drawImage(canvas, 0, y, canvas.width, h, 0, 0, canvas.width, h);
      results.push(c);
    }
  } else {
    const partW = Math.floor(canvas.width / numParts);
    const rem   = canvas.width % numParts;
    for (let i = 0; i < numParts; i++) {
      const x = i * partW;
      const w = partW + (i === numParts - 1 ? rem : 0);
      const c = safeCanvas(w, canvas.height);
      if (!c) throw new Error('Split part too large for canvas.');
      c.getContext('2d').drawImage(canvas, x, 0, w, canvas.height, 0, 0, w, canvas.height);
      results.push(c);
    }
  }
  return results;
}

// ═══════════════════════════════════════════════════════
//  VIRTUAL SMART SPLIT
//  Processes images one-by-one — never creates a giant canvas.
//  Works for any number of images / any total height.
// ═══════════════════════════════════════════════════════

// Scan a single image for uniform rows starting at or after `nextSplitY`
// (in global coordinates). Returns updated split list and nextSplitY.
function scanImageRows(imgData, iw, ih, globalOffset, nextSplitY, startH, splits) {
  for (let row = 0; row < ih; row++) {
    const absY = globalOffset + row;
    if (absY < nextSplitY) continue;

    // Check if every adjacent pixel pair in this row has similar colour
    let uniform = true;
    for (let j = 0; j < iw - 1; j++) {
      const p1 = (row * iw + j)     * 4;
      const p2 = (row * iw + j + 1) * 4;
      if (Math.abs(imgData[p1]   - imgData[p2])   > SMART_TOL ||
          Math.abs(imgData[p1+1] - imgData[p2+1]) > SMART_TOL ||
          Math.abs(imgData[p1+2] - imgData[p2+2]) > SMART_TOL ||
          Math.abs(imgData[p1+3] - imgData[p2+3]) > SMART_TOL) {
        uniform = false;
        break;
      }
    }

    if (uniform) {
      splits.push(absY);
      nextSplitY = absY + startH;
    }
  }
  return nextSplitY;
}

// Render one vertical section [startY, endY] by compositing source images
function renderSectionVertical(imgs, startY, endY) {
  const maxW = Math.max(...imgs.map(imgW));
  const h    = endY - startY;
  const c    = safeCanvas(maxW, h);
  if (!c) throw new Error(`Section too large (${maxW}×${h}).`);

  const ctx = c.getContext('2d');
  let imgStartY = 0;

  for (const img of imgs) {
    const iH = imgH(img);
    const iW = imgW(img);
    const imgEndY = imgStartY + iH;

    if (imgEndY > startY && imgStartY < endY) {
      const clipTop = Math.max(imgStartY, startY);
      const clipBot = Math.min(imgEndY, endY);
      const srcY    = clipTop - imgStartY;
      const dstY    = clipTop - startY;
      const segH    = clipBot - clipTop;
      ctx.drawImage(img, 0, srcY, iW, segH, 0, dstY, iW, segH);
    }

    imgStartY = imgEndY;
    if (imgStartY >= endY) break;
  }
  return c;
}

// Main virtual smart-split function — works on array of Images
async function smartSplitVirtual(imgs, numParts, progressCb) {
  const totalH = imgs.reduce((s, i) => s + imgH(i), 0);
  const startH = Math.floor(totalH / numParts);

  const splits  = [0];
  let nextSplitY = startH;
  let globalOffset = 0;

  for (let idx = 0; idx < imgs.length; idx++) {
    const img = imgs[idx];
    const iw  = imgW(img);
    const ih  = imgH(img);

    // Render this single image to a small canvas for pixel scanning
    const tmp = safeCanvas(iw, ih);
    if (!tmp) { globalOffset += ih; continue; } // skip if this single image is somehow too large
    tmp.getContext('2d').drawImage(img, 0, 0);
    const data = tmp.getContext('2d').getImageData(0, 0, iw, ih).data;

    nextSplitY = scanImageRows(data, iw, ih, globalOffset, nextSplitY, startH, splits);

    globalOffset += ih;
    if (progressCb) progressCb(Math.round((globalOffset / totalH) * 80));
    await yield_();
  }

  if (splits[splits.length - 1] !== totalH) splits.push(totalH);
  return splits; // array of global Y split points
}

// ═══════════════════════════════════════════════════════
//  SMART SPLIT — single canvas version (for small images)
// ═══════════════════════════════════════════════════════
async function smartSplitCanvas(canvas, numParts, progressCb) {
  const W  = canvas.width;
  const H  = canvas.height;
  const data = canvas.getContext('2d').getImageData(0, 0, W, H).data;

  const startH = Math.floor(H / numParts);
  const splits = [0];
  let nextSplitY = startH;

  for (let i = startH; i < H; i++) {
    let uniform = true;
    for (let j = 0; j < W - 1; j++) {
      const p1 = (i * W + j)     * 4;
      const p2 = (i * W + j + 1) * 4;
      if (Math.abs(data[p1]   - data[p2])   > SMART_TOL ||
          Math.abs(data[p1+1] - data[p2+1]) > SMART_TOL ||
          Math.abs(data[p1+2] - data[p2+2]) > SMART_TOL ||
          Math.abs(data[p1+3] - data[p2+3]) > SMART_TOL) {
        uniform = false;
        break;
      }
    }
    if (uniform) {
      splits.push(i);
      i += startH;
    }
    if (i % 300 === 0) {
      if (progressCb) progressCb(Math.round((i / H) * 80));
      await yield_();
    }
  }

  if (splits[splits.length - 1] !== H) splits.push(H);
  return splits;
}

// Convert split-point array → array of canvases (single-canvas source)
function splitPointsToCanvases(canvas, splits) {
  const results = [];
  for (let i = 0; i < splits.length - 1; i++) {
    const y = splits[i];
    const h = splits[i + 1] - y;
    if (h <= 0) continue;
    const c = safeCanvas(canvas.width, h);
    if (c) {
      c.getContext('2d').drawImage(canvas, 0, y, canvas.width, h, 0, 0, canvas.width, h);
      results.push(c);
    }
  }
  return results;
}

// ═══════════════════════════════════════════════════════
//  WATERMARK
// ═══════════════════════════════════════════════════════
function applyWatermark(canvas, wmImg, opacityPct, greyscale, count, widthPct) {
  const ctx = canvas.getContext('2d');
  const W   = canvas.width;
  const H   = canvas.height;

  const wmW = Math.max(1, Math.floor(W * widthPct / 100));
  const wmH = Math.max(1, Math.floor(wmW / (imgW(wmImg) / imgH(wmImg))));

  const wm  = safeCanvas(wmW, wmH);
  if (!wm) return canvas;
  const wctx = wm.getContext('2d');
  wctx.drawImage(wmImg, 0, 0, wmW, wmH);

  if (greyscale) {
    const id = wctx.getImageData(0, 0, wmW, wmH);
    for (let i = 0; i < id.data.length; i += 4) {
      const g = 0.299 * id.data[i] + 0.587 * id.data[i+1] + 0.114 * id.data[i+2];
      id.data[i] = id.data[i+1] = id.data[i+2] = g;
    }
    wctx.putImageData(id, 0, 0);
  }

  ctx.save();
  ctx.globalAlpha = opacityPct / 100;
  const sectionH = Math.floor(H / count);
  for (let i = 1; i <= count; i++) {
    ctx.drawImage(wm, W - Math.floor(wmW * 1.1), H - sectionH * i);
  }
  ctx.restore();
  return canvas;
}

// ═══════════════════════════════════════════════════════
//  DOWNLOAD
// ═══════════════════════════════════════════════════════
async function downloadSingle(canvas, filename, format) {
  const blob = await canvasToBlob(canvas, format);
  const ext  = format === 'jpeg' ? 'jpg' : format;
  const a    = document.createElement('a');
  a.href     = URL.createObjectURL(blob);
  a.download = `${filename}.${ext}`;
  a.click();
  setTimeout(() => URL.revokeObjectURL(a.href), 60000);
}

async function downloadAll(canvases, basename, format) {
  if (canvases.length === 1) {
    await downloadSingle(canvases[0], basename, format);
    return;
  }
  const ext = format === 'jpeg' ? 'jpg' : format;
  if (typeof JSZip !== 'undefined') {
    const zip = new JSZip();
    for (let i = 0; i < canvases.length; i++) {
      const blob = await canvasToBlob(canvases[i], format);
      zip.file(`${basename}_${i + 1}.${ext}`, blob);
    }
    const zipBlob = await zip.generateAsync({ type: 'blob' });
    const a = document.createElement('a');
    a.href     = URL.createObjectURL(zipBlob);
    a.download = `${basename}.zip`;
    a.click();
    setTimeout(() => URL.revokeObjectURL(a.href), 60000);
  } else {
    // Fallback: sequential individual downloads
    for (let i = 0; i < canvases.length; i++) {
      await downloadSingle(canvases[i], `${basename}_${i + 1}`, format);
      await new Promise(r => setTimeout(r, 350));
    }
  }
}

// ═══════════════════════════════════════════════════════
//  RUN
// ═══════════════════════════════════════════════════════
async function run() {
  const mode  = state.mode;
  const fmt   = dom.fmtSelect.value;
  const name  = dom.filename.value.trim() || 'output';
  const dir   = state.direction;
  const parts = Math.max(2, parseInt(dom.partsInput.value) || 2);

  // ── Validation ──────────────────────────────────────
  if (mode !== 'watermark' && state.files.length === 0) {
    alert('Please import at least one image.'); return;
  }
  if ((mode === 'stitch' || mode === 'stitchsplit') && state.files.length < 2) {
    alert('Stitch requires at least 2 images.'); return;
  }
  if (mode === 'watermark' && !state.wmFile) {
    alert('Please import a watermark image.'); return;
  }

  dom.runBtn.disabled = true;
  hideResult();
  showProgress('Loading images…');

  try {
    // ── Load all images ──────────────────────────────
    const imgs = [];
    for (let i = 0; i < state.files.length; i++) {
      setProgress(Math.round((i / state.files.length) * 25), `Loading ${i + 1} / ${state.files.length}…`);
      imgs.push(await loadImage(state.files[i]));
      if (i % 5 === 0) await yield_();
    }

    let results = [];

    // ════════════════════════════════════════════════
    //  STITCH — output a single stitched image
    // ════════════════════════════════════════════════
    if (mode === 'stitch') {
      setProgress(30, 'Calculating dimensions…');
      await yield_();

      const { scale, totalW, totalH } = computeScale(imgs, dir);

      if (scale < 1) {
        const pct = Math.round(scale * 100);
        if (!confirm(
          `The stitched image would be ${totalW}×${totalH}px which exceeds browser limits.\n\n` +
          `Images will be scaled to ${pct}% (${Math.round(totalW * scale)}×${Math.round(totalH * scale)}px).\n\n` +
          `Continue?`
        )) {
          hideProgress();
          dom.runBtn.disabled = false;
          return;
        }
      }

      setProgress(50, 'Stitching…');
      await yield_();
      results = [stitchImages(imgs, dir, scale)];
      setProgress(100, 'Done!');
    }

    // ════════════════════════════════════════════════
    //  SPLIT — equal parts of a single image
    // ════════════════════════════════════════════════
    else if (mode === 'split') {
      setProgress(30, 'Preparing…');
      await yield_();

      const { scale } = computeScale(imgs, 'vertical');
      if (scale < 1 && !confirm(
        `Image is very large. It will be scaled to ${Math.round(scale * 100)}% before splitting. Continue?`
      )) {
        hideProgress(); dom.runBtn.disabled = false; return;
      }

      setProgress(50, 'Splitting…');
      await yield_();
      const canvas = stitchImages(imgs, 'vertical', scale); // single image → canvas
      results = splitCanvas(canvas, parts, dir);
      setProgress(100, 'Done!');
    }

    // ════════════════════════════════════════════════
    //  SMART SPLIT — auto-detect uniform rows
    //  Uses virtual processing — no size limit!
    // ════════════════════════════════════════════════
    else if (mode === 'smartsplit') {
      setProgress(25, 'Scanning rows…');

      const totalH  = imgs.reduce((s, i) => s + imgH(i), 0);
      const totalW  = Math.max(...imgs.map(imgW));
      const isLarge = !isWithinLimits(totalW, totalH);

      let splitPts;

      if (isLarge) {
        // Virtual path — scan each image independently
        splitPts = await smartSplitVirtual(imgs, parts, pct => setProgress(pct, 'Scanning rows…'));
      } else {
        // Small enough — load into one canvas and scan
        const canvas = stitchImages(imgs, 'vertical');
        splitPts = await smartSplitCanvas(canvas, parts, pct => setProgress(pct, 'Scanning rows…'));
      }

      if (splitPts.length < 2) {
        hideProgress();
        alert('Smart Split could not find any uniform split lines.\nTry adjusting the "Parts" count.');
        dom.runBtn.disabled = false;
        return;
      }

      setProgress(85, `Rendering ${splitPts.length - 1} sections…`);
      await yield_();

      if (isLarge) {
        // Render sections by compositing source images
        for (let i = 0; i < splitPts.length - 1; i++) {
          setProgress(85 + Math.round((i / (splitPts.length - 1)) * 14), `Rendering section ${i + 1}…`);
          results.push(renderSectionVertical(imgs, splitPts[i], splitPts[i + 1]));
          await yield_();
        }
      } else {
        const canvas = stitchImages(imgs, 'vertical');
        results = splitPointsToCanvases(canvas, splitPts);
      }

      setProgress(100, 'Done!');
    }

    // ════════════════════════════════════════════════
    //  STITCH + SPLIT — the primary large-image workflow
    //  Always uses virtual processing.
    // ════════════════════════════════════════════════
    else if (mode === 'stitchsplit') {
      const combo     = dom.ssCombo.value;             // e.g. "v-smart"
      const stitchDir = combo.startsWith('v') ? 'vertical' : 'horizontal';
      const splitOp   = combo.split('-')[1];            // smart | h | v

      const totalH = imgs.reduce((s, i) => s + imgH(i), 0);
      const totalW = stitchDir === 'vertical'
        ? Math.max(...imgs.map(imgW))
        : imgs.reduce((s, i) => s + imgW(i), 0);
      const isLarge = !isWithinLimits(totalW, totalH);

      if (splitOp === 'smart' && stitchDir === 'vertical' && isLarge) {
        // ── Fully virtual path (most common use case) ──
        setProgress(25, 'Scanning rows (virtual)…');

        const splitPts = await smartSplitVirtual(imgs, parts, pct =>
          setProgress(pct, `Scanning rows… (${pct}%)`));

        if (splitPts.length < 2) {
          hideProgress();
          alert('Smart Split could not find any split lines in the stitched image.');
          dom.runBtn.disabled = false;
          return;
        }

        setProgress(85, `Rendering ${splitPts.length - 1} sections…`);
        await yield_();

        for (let i = 0; i < splitPts.length - 1; i++) {
          setProgress(85 + Math.round((i / (splitPts.length - 1)) * 14), `Rendering section ${i + 1}…`);
          results.push(renderSectionVertical(imgs, splitPts[i], splitPts[i + 1]));
          await yield_();
        }

      } else {
        // ── Canvas path (small images or horizontal stitch) ──
        const { scale } = computeScale(imgs, stitchDir);
        if (scale < 1 && !confirm(
          `Combined image is too large. Will scale to ${Math.round(scale*100)}%. Continue?`
        )) {
          hideProgress(); dom.runBtn.disabled = false; return;
        }

        setProgress(30, 'Stitching…');
        await yield_();
        const stitched = stitchImages(imgs, stitchDir, scale);

        if (splitOp === 'smart') {
          setProgress(50, 'Smart splitting…');
          const splitPts = await smartSplitCanvas(stitched, parts, pct =>
            setProgress(50 + Math.round(pct * 0.4), 'Scanning rows…'));
          if (splitPts.length < 2) {
            hideProgress();
            alert('Smart Split could not find any split lines in the stitched image.');
            dom.runBtn.disabled = false;
            return;
          }
          results = splitPointsToCanvases(stitched, splitPts);
        } else {
          setProgress(70, 'Splitting…');
          await yield_();
          const splitDir = splitOp === 'h' ? 'horizontal' : 'vertical';
          results = splitCanvas(stitched, parts, splitDir);
        }
      }

      setProgress(100, 'Done!');
    }

    // ════════════════════════════════════════════════
    //  WATERMARK
    // ════════════════════════════════════════════════
    else if (mode === 'watermark') {
      setProgress(30, 'Loading watermark…');
      const wmImg = await loadImage(state.wmFile);

      const { scale } = computeScale(imgs, 'vertical');
      if (scale < 1 && !confirm(
        `Image is very large. Will scale to ${Math.round(scale*100)}%. Continue?`
      )) {
        hideProgress(); dom.runBtn.disabled = false; return;
      }

      setProgress(60, 'Applying watermark…');
      await yield_();

      const src    = imgs[0];
      const sw     = Math.round(imgW(src) * scale);
      const sh     = Math.round(imgH(src) * scale);
      const canvas = safeCanvas(sw, sh);
      if (!canvas) throw new Error('Image too large for canvas.');
      canvas.getContext('2d').drawImage(src, 0, 0, sw, sh);

      applyWatermark(
        canvas, wmImg,
        parseInt(dom.opacityRange.value) || 40,
        dom.greyscaleChk.checked,
        Math.max(1, parseInt(dom.wmCount.value) || 3),
        Math.min(100, Math.max(1, parseInt(dom.wmWidthPct.value) || 20))
      );
      results = [canvas];
      setProgress(100, 'Done!');
    }

    await yield_();
    hideProgress();
    showResult(results);

  } catch (err) {
    hideProgress();
    console.error(err);
    alert('Error: ' + err.message);
  } finally {
    dom.runBtn.disabled = false;
  }
}

// ─── Size check helper ───────────────────────────────
function isWithinLimits(w, h) {
  return w <= MAX_CANVAS_DIM && h <= MAX_CANVAS_DIM && w * h <= MAX_CANVAS_PIXELS;
}

// ═══════════════════════════════════════════════════════
//  Event wiring
// ═══════════════════════════════════════════════════════

dom.tabs.forEach(tab => tab.addEventListener('click', () => applyMode(tab.dataset.mode)));

[dom.btnVertical, dom.btnHorizontal].forEach(btn => {
  btn.addEventListener('click', () => {
    state.direction = btn.dataset.dir;
    dom.btnVertical.classList.toggle('active',   state.direction === 'vertical');
    dom.btnHorizontal.classList.toggle('active', state.direction === 'horizontal');
  });
});

dom.opacityRange.addEventListener('input', () => {
  dom.opacityVal.textContent = dom.opacityRange.value;
});

dom.fileInput.addEventListener('change', e => {
  const incoming = Array.from(e.target.files || []);
  state.files = dom.fileInput.multiple ? [...state.files, ...incoming] : incoming.slice(0, 1);
  renderFileList();
  e.target.value = '';
});

dom.dropZone.addEventListener('dragover',  e => { e.preventDefault(); dom.dropZone.classList.add('drag-over'); });
dom.dropZone.addEventListener('dragleave', ()  => dom.dropZone.classList.remove('drag-over'));
dom.dropZone.addEventListener('drop', e => {
  e.preventDefault();
  dom.dropZone.classList.remove('drag-over');
  const files = Array.from(e.dataTransfer.files).filter(f => f.type.startsWith('image/'));
  state.files = dom.fileInput.multiple ? [...state.files, ...files] : files.slice(0, 1);
  renderFileList();
});

dom.dropZone.addEventListener('keydown', e => {
  if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); dom.fileInput.click(); }
});

dom.wmImgBtn.addEventListener('click', () => dom.wmImgInput.click());
dom.wmImgInput.addEventListener('change', e => {
  const file = e.target.files[0];
  if (file) { state.wmFile = file; dom.wmImgLabel.textContent = file.name; }
  e.target.value = '';
});

dom.runBtn.addEventListener('click', run);

dom.downloadBtn.addEventListener('click', async () => {
  if (!state.results.length) return;
  dom.downloadBtn.disabled = true;
  dom.downloadBtn.textContent = '⏳ Preparing…';
  try {
    await downloadAll(state.results, dom.filename.value.trim() || 'output', dom.fmtSelect.value);
  } finally {
    dom.downloadBtn.disabled = false;
    dom.downloadBtn.textContent = '⬇ Download';
  }
});

// ═══════════════════════════════════════════════════════
//  Init
// ═══════════════════════════════════════════════════════
applyMode('stitch');
