# rapid-downloader

while true; do sleep 60 && mv -v $(find ./ -name '*.rar' -mmin +1 -mmin -60) /tmp; done
