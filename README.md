<p align="center"><img src="https://i.imgur.com/QwItpe6.png" width="200" height="200"/></p>

<h1 align="center">StitchTool</h1>

<font size="3"><a href="https://github.com/Aeonss/StitchTool/releases/latest/">StitchTool</a> is simple and intuitive program for webtoons creators and translation groups to stitch or
split large quantities of images. It has a variety of features and tools to make this process easier, such as a <strong><u>smart
splitter</u></strong> and an <strong><u>integrated denoiser and upscaler</u></strong>.</font>

<br>
<p align="center">
<a href="https://github.com/Aeonss/StitchTool/releases/latest/"><img src="https://img.shields.io/github/v/release/Aeonss/StitchTool?style=for-the-badge&label=%20%F0%9F%93%A3%20Latest%20release&color=778beb&labelColor=2f3542"/></a>
<img src="https://img.shields.io/github/stars/Aeonss/StitchTool?style=for-the-badge&label=%E2%AD%90%20Stars&color=786fa6&labelColor=2f3542"/>
<img src="https://img.shields.io/github/downloads/Aeonss/StitchTool/total.svg?style=for-the-badge&label=%E2%AC%87%EF%B8%8FDownloads&color=4b6584&labelColor=2f3542"/>
</p>

<br>
<p align="center"><img src="https://i.imgur.com/2i5lcxn.png" width="350" height="450"/></p>


<font size="3">

## 🚀 &nbsp; Features
* Stitch together **any** amount of images either vertically or horizontally.
* Split an image into **any** amount of images either vertically or horizontally.
* Preview image before splitting, which is shown by a red line in a separate window.
* Smart split images by free space, allowing for <b><u>automatic splitting</u></b> without needing to check if the image
  was split through a speech bubble or SFX.
* Add repeatable watermarks to your images.
* Supports [Waifu2X Caffe](https://github.com/lltcggie/waifu2x-caffe/releases/latest/) and [Waifu2X Vulkan](https://github.com/nihui/waifu2x-ncnn-vulkan/releases/latest) integration.
* <b><u>Denoise and/or upscale</u></b> images in the program, or while stitching or splitting.
* Supports .png, .jpg, .jpeg, and .webp!

## 🔨 &nbsp; Installation
* Make sure you have [Java 8 or above](https://jdk.java.net/archive/) installed.
* Download the or jar file from the [latest release](https://github.com/Aeonss/StitchTool/releases/latest/) and run it with Java.
* (Windows Only) Alternatively, download the [exe file](https://github.com/Aeonss/StitchTool/releases/latest/) and run it. If you get a Windows Defender message, click "more info" and then "run".
* Read through the [code](https://github.com/Aeonss/StitchTool/tree/master/src) or scan with [VirusTotal](https://virustotal.com) if you wish.
* To import Waifu2X into StitchTool, download one of the forks mentioned above.
* Select the pink "Import Waifu2X button", and select the executable Waifu2X file.
* Make sure to select the CUI.exe if you're using [Waifu2X Caffe](https://github.com/lltcggie/waifu2x-caffe/releases/latest/).

## 📝 &nbsp; How To Use

<h3>Stitching and Splitting</h3>
<ol>
<li> Select the "Stitch/Split" teal radio button. </li>
<li> From the blue dropdown menu, select the specific option you want. </li>
<li> Select the input images you want to stitch and split with the green button. You can choose an entire folder of images, or you can choose specific images. </li>
<li> Select output location. If not selected, it defaults to the user's folder. </li>
<li> Input the image name in the red textbox. If not inputted, it generates a random 10 character name. </li>
<li> (Optional) Import the location of <b><u>waifu2x-caffe-cui.exe</u></b> or <b><u>waifu2x-ncnn-vulkan</u></b> by clicking the pink "Import Waifu2X" button to denoise and/or scale during the process. </li>
<li> Click the run button at the bottom. </li>
<li> Choose the amount of images to be split into. </li>
</ol>

<h3>Stitching</h3>
<ol>
<li> Select the "Stitch" teal radio button. </li>
<li> Select the "Vertical" or "Horizontal" blue radio button. </li>
<li> Select the input images you want to stitch with the green button. You can choose an entire folder of images, or you can choose specific images. </li>
<li> Select output location with the green "Browse Output Location" button. If not selected, it defaults to the user's folder. </li>
<li> Input the image name in the red textbox. If not inputted, it generates a random 10 character name. </li>
<li> (Optional) Import the location of <b><u>waifu2x-caffe-cui.exe</u></b> or <b><u>waifu2x-ncnn-vulkan</u></b> by clicking the pink "Import Waifu2X" button to denoise and/or scale during the process. </li>
<li> Click the run button at the bottom. </li>
</ol>

<h3>Splitting</h3>
<ol>
<li> Select the "Split" teal radio button. </li>
<li> From the blue dropdown menu, select the specific option you want. </li>
<li> Select the folder of images you want to split with the green button. </li>
<li> Select output location with the green "Browse Output Location" button. If not selected, it defaults to the user's folder. </li>
<li> Input the image name in the red textbox. If not inputted, it defaults to "StitchTool-". </li>
<li> (Optional) Import the location of <b><u>waifu2x-caffe-cui.exe</u></b> or <b><u>waifu2x-ncnn-vulkan</u></b> by clicking the pink "Import Waifu2X" button to denoise and/or scale during the process. </li>
<li> Click the run button at the bottom. </li>
<li> Choose the amount of images to be split into. </li>
<li> Review the preview and continue or reenter the number of images you want the image to be split into. </li>
</ol>

<h3>Smart Splitting</h3>
<ol>
<li> Select the "Split" teal radio button. </li>
<li> From the blue dropdown menu, select the specific option you want. </li>
<li> Select the folder of images you want to split. </li>
<li> Select output location with the green "Browse Output Location" button. If not selected, it defaults to the user's folder. </li>
<li> Input the image name in the red textbox. If not inputted, it defaults to "StitchTool-". </li>
<li> (Optional) Import the location of <b><u>waifu2x-caffe-cui.exe</u></b> or <b><u>waifu2x-ncnn-vulkan</u></b> by clicking the pink "Import Waifu2X" button to denoise and/or scale during the process. </li>
<li> Click the run button at the bottom. </li>
</ol>

<h3>Denoising and/or Upscaling</h3>
<ol>
<li> Input the image name in the red textbox. If not inputted, it defaults to "StitchTool-". </li>
<li> Import the location of <b><u>waifu2x-caffe-cui.exe</u></b> or <b><u>waifu2x-ncnn-vulkan</u></b> by clicking the pink "Import Waifu2X" button. </li>
<li> Select the denoising level and/or scale ratio OR scale height/width. </li>
<li> Waifu2x prioritizes scale height/width if the ratio and dimensions are given. </li>
<li> Click the pink "Run Waifu2X" button. </li>
<li> Click the run button at the bottom. </li>
</ol>

<h3>Watermarking</h3>
<ol>
<li> Select output location with the green "Browse Output Location" button. </li>
<li> Input the image name in the red textbox. If not inputted, it generates a random 10 character name. </li>
<li> Select the opacity of the watermark using the opacity slider. </li>
<li> Select if you want the watermark to be greyscale or not with the yellow checkbox. </li>
<li> Click the yellow watermark button. </li>
<li> Select the image you want the watermark to be on. </li>
<li> Select the watermark image. </li>
<li> Input the number of times you want the watermark to appear. </li>
</ol>

## ✅ &nbsp; Additional Information
* StitchTool was compiled with **Java 17** and **JavaFX 17**.
* StitchTool will stitch together images that are named in *alphanumerical order*.
* Collapse sections with the arrow button next to the line separator.
* Waifu2X Caffe prioritizes **scale height and width over scale ratio**, and outputs only in PNG for the best quality.
* Please request any features or report any bugs in [issues](https://github.com/Aeonss/StitchTool/issues).

## ❤️&nbsp; Contributions
* Thank you to ZeroCool940711 for helping debugging and testing StitchTool
* Thank you to thenuke740 and mjsaltus for UI suggestions

## 📘 &nbsp; License
StitchTool is released under the [MIT license](https://github.com/Aeonss/StitchTool/blob/master/LICENSE.md).

</font>
