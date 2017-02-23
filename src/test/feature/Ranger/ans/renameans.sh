#!/bin/bash

for  i in `ls|grep ans|grep admin`
do
    newName=`echo $i | sed 's/adminsecond/adminfirst/g'`
    cp $i $newName
done
