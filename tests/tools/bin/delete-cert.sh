#!/bin/bash
certutil -D -d /root/.dogtag/nssdb -n $1
certutil -L -d /root/.dogtag/nssdb
certutil -K -d /root/.dogtag/nssdb
