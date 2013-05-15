#!/bin/sh
dd if=/dev/zero of=ramdisk.img bs=1024 count=32768
echo "------------dd------------"
mke2fs -F ramdisk.img -L "ramdisk" -b 1024 -m 0
echo "------------mke2fs------------"
tune2fs ramdisk.img -i 0
echo "------------tune2fs------------"
chmod 777 ramdisk.img
echo "-----------chmod------------"

mkdir ramdisk
echo "-----------mkdir------------"
sudo mount -o loop ramdisk.img ramdisk/
echo "-----------mount------------"
sudo cp -R _rootfs/* ramdisk
echo "-----------cp------------"
sudo umount ramdisk/
echo "-----------umount------------"

sudo gzip ramdisk.img
echo "-----------gzip------------"
sudo mv ./ramdisk.img.gz ./ramdisk32M.image.gz
