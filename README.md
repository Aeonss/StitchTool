<p align="center"><img src="https://i.imgur.com/QwItpe6.png" width="200" height="200"/></p>

<h1 align="center">StitchTool</h1>

<font size="3"><a href="https://github.com/Aeonss/StitchTool/releases/latest/">StitchTool</a> is simple and intuitive program for webtoons creators and translation groups to stitch or
split large quantities of images. It has a variety of features and tools to make this process easier, such as a <strong><u>smart
splitter</u></strong> and <strong><u>integrated denoiser and upscaler</u></strong>.</font>

<br>
<p align="center">
<a href="https://github.com/Aeonss/StitchTool/releases/latest/"><img src="https://img.shields.io/github/v/release/Aeonss/StitchTool?style=for-the-badge&label=%20%F0%9F%93%A3%20Latest%20release&color=778beb&labelColor=2f3542"/></a>
<img src="https://img.shields.io/github/stars/Aeonss/StitchTool?style=for-the-badge&label=%E2%AD%90%20Stars&color=786fa6&labelColor=2f3542"/>
<img src="https://img.shields.io/github/downloads/Aeonss/StitchTool/total.svg?style=for-the-badge&label=%E2%AC%87%EF%B8%8FDownloads&color=4b6584&labelColor=2f3542"/>
</p>

<br>
<p align="center"><img src="https://i.imgur.com/2i5lcxn.png" width="350" height="450"/></p>


<font size="3">

## 🔨 &nbsp; Installation
* Make sure you have [Java 8 or above](https://jdk.java.net/archive/) installed.
* Download the jar file from the <a href="https://github.com/Aeonss/StitchTool/releases/latest/">latest release</a> and run it with Java.
* Any denoising or upscaling requires [Waifu2X-Caffe](https://github.com/lltcggie/waifu2x-caffe/releases/latest/).

## 🚀 &nbsp; Features
* Stitch together **any** amount of images either vertically or horizontally.
* Split an image into **any** amount of images either vertically or horizontally.
* Preview image before splitting, which is shown by a red line in a separate window.
* Smart split images by free space, allowing for **<u>automatic splitting</u>** without needing to check if the image
was split through a speech bubble or SFX.
* Add repeatable watermarks to your images.
* **<u>Denoise and/or upscale</u>** images while stitching or splitting.
* Supports .png, .jpg, .jpeg, and .webp!

## 📝 &nbsp; How To Use

<h3>Stitching and Splitting</h3>
1. Select the "Stitch/Split" teal radio button 
2. From the blue dropdown menu, select the specific option you want.
3. Select the input images you want to stitch and split. You can choose an entire folder of images, or you can choose specific images.
4. Select output location. If not selected, it defaults to the user's folder.
5. Input the image name in the red textbox. If not inputted, it generates a random 10 character name.
6. (Optional) Import the location of **<u>waifu2x-caffe-cui.exe</u>** by clicking the pink "Import Waifu2X" button to denoise and/or scale during the process.
7. Click the run button at the bottom.
8. Choose the amount of images to be split into.

<h3>Stitching</h3>
1. Select the "Stitch" teal radio button
2. Select the "Vertical" or "Horizontal" blue radio button
3. Select the input images you want to stitch. You can choose an entire folder of images, or you can choose specific images.
4. Select output location with the green "Browse Output Location" button. If not selected, it defaults to the user's folder.
5. Input the image name in the red textbox. If not inputted, it generates a random 10 character name.
6. (Optional) Import the location of **<u>waifu2x-caffe-cui.exe</u>** by clicking the pink "Import Waifu2X" button to denoise and/or scale during the process.
7. Click the run button at the bottom.

<h3>Splitting</h3>
1. Select the "Split" teal radio button
2. From the blue dropdown menu, select the specific option you want.
3. Select the folder of images you want to split.
4. Select output location with the green "Browse Output Location" button. If not selected, it defaults to the user's folder.
5. Input the image name in the red textbox. If not inputted, it defaults to "StitchTool-".
6. (Optional) Import the location of **<u>waifu2x-caffe-cui.exe</u>** by clicking the pink "Import Waifu2X" button to denoise and/or scale during the process.
7. Click the run button at the bottom.
8. Choose the amount of images to be split into.
9. Review the preview and continue or reenter the number of images you want the image to be split into.

<h3>Smart Splitting</h3>
1. Select the "Split" teal radio button
2. From the blue dropdown menu, select the specific option you want.
3. Select the folder of images you want to split.
4. Select output location with the green "Browse Output Location" button. If not selected, it defaults to the user's folder.
5. Input the image name in the red textbox. If not inputted, it defaults to "StitchTool-".
6. (Optional) Import the location of **<u>waifu2x-caffe-cui.exe</u>** by clicking the pink "Import Waifu2X" button to denoise and/or scale during the process.
7. Click the run button at the bottom.

<h3>Denoising and/or Upscaling</h3>
1. Input the image name in the red textbox. If not inputted, it defaults to "StitchTool-".
2. Import the location of **<u>waifu2x-caffe-cui.exe</u>** by clicking the pink "Import Waifu2X" button.
3. Select the denoising level and/or scale ratio OR scale height/width
4. Waifu2x prioritizes scale height/width if the ratio and dimensions are given.
5. Click the pink "Run Waifu2X" button.
6. Click the run button at the bottom.

<h3>Watermarking</h3>
1. Select output location with the green "Browse Output Location" button
2. Input the image name in the red textbox. If not inputted, it generates a random 10 character name.
3. Select the opacity of the watermark using the opacity slider.
4. Select if you want the watermark to be greyscale or not with the yellow checkbox.
5. Click the yellow watermark button.
6. Select the image you want the watermark to be on.
7. Select the watermark image.
8. Input the number of times you want the watermark to appear.

## ✅ &nbsp; Additional Information
* StitchTool was compiled with **Java 17** and **JavaFX 17**.
* StitchTool will stitch together images that are named in *alphanumerical order*.
* Collapse sections with the arrow button next to the line separator.
* Waifu2X Caffe prioritizes **scale height and width over scale ratio**.
* Please request any features or report any bugs in [issues](https://github.com/Aeonss/StitchTool/issues).

## 📘 &nbsp; License
StitchTool is released under the [MIT license](https://github.com/Aeonss/StitchTool/blob/master/LICENSE.md).

</font>
