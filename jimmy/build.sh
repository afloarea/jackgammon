#!/usr/bin/env bash

sed -i "s~wss\?.*\w~$JACKGAMMON_BACKEND_ENDPOINT~" index.js
