#!/bin/bash
# $1 - IP Coordinador
# $2 - Puerto servidor
# $3 - Numero de clientes (Very important)
java cl/uchile/dcc/cc5303/Server -ipc $1 -p $2 -n $3

