#!/bin/bash


mkdir -p bin
rm -rf bin/*

jar cmf 'manifest.mf' bin/ApproachControl.jar -C obj/ .
