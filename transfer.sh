#!/bin/bash

while true
do 

export files=$(find ./ -name '*.rar' -mmin +1 -mmin -60

if [ "$files" == "" ]
then
echo "No files to transfer.";
else
echo "Files to transfer -> $files";
export m=$(udisksctl mount -b /dev/sdb1);
export a=(${m//at/ })
mv -v $files ${a[2]}/gfx;
udisksctl unmount -b /dev/sdb1 && hdparm -y /dev/sdb1;
fi

sleep 900;

done
