#!/bin/bash
# Sample call batch-replace.sh /uv/dpusToImport

REST_API_URL="http://localhost:8080/master/api/1/import/dpu/jar"
MASTER_USER=admin    
MASTER_PASS=test

echo "---------------------------------------------------------------------"
echo "Replacing DPUs using REST API URL: $REST_API_URL"
echo "---------------------------------------------------------------------"

install_dpu() {
    dpu_file=$(ls $1)

    echo -n "Replacing DPU $dpu_file: "
    outputfile="/tmp/dpu_out.out"

    # fire cURL and wait until it finishes
    curl --user $MASTER_USER:$MASTER_PASS --fail --silent --output $outputfile -X POST -H "Cache-Control: no-cache" -H "Content-Type: multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW" -F file=@$dpu_file $REST_API_URL?force=true 
    wait $!

    # check if the installation went well
    outcontents=`cat $outputfile`
    echo $outcontents
}

for filename in `ls $1`; do
    echo $filename
    install_dpu $1/$filename
done;

#install_dpu "dpus/uv-t-filesFilter-*.jar"

