#!/bin/bash

curl -H "Content-Type: application/json" \
     -H "Authorization: key=$KEY" \
     -H "Content-type: application/json" \
     -d "{\"to\":\"$DEVICEID\", \"notification\": {\"title\":\"CPU above 80%\", \"icon\":\"clean.png\", \"click_action\":\"https://localhost:8444/#/alert-details/view/1\"}}" \
     -v -X POST https://gcm-http.googleapis.com/gcm/send
