32MB_ZRobot_Image.tar.bz2 is basic image for ZRobot.Untar it to FAT partition to boot ZRobot.



_rootfs is ramdisk filesystem package.

1.Copy your design(be cross-compiled) into filesystem.
2.run mk_ramdisk.sh shell script to auto build the 32MB filesystem image.
3.Replace it to ramdisk32M.image.gz in the FAT partition.
4.After boot up, use console to run your design.



XUP
5/15/2013

