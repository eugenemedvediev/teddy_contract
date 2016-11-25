#!/usr/bin/env bash

if [ -z "$1" ];
    then echo "missing parameter service port. ex: 8000"; exit;
    else servicePort=$1;
fi

if [ -z "$2" ];
    then echo "missing parameter contract port range. ex: 9000-9005"; exit;
    else portsRange=$2;
    IFS='-' read -ra ports <<< "$portsRange";
    fromPort=${ports[0]};
    toPort=${ports[1]};
fi

dockerCmdD="docker run -d -p ${servicePort}:8000 --expose=$fromPort-$toPort -p $fromPort-$toPort:$fromPort-$toPort -e \"FROM_PORT=${fromPort}\" -e \"TO_PORT=${toPort}\" imedvediev/api-contract:0.0.1"
dockerCmdF="docker run -it -p ${servicePort}:8000 --expose=$fromPort-$toPort -p $fromPort-$toPort:$fromPort-$toPort -e \"FROM_PORT=${fromPort}\" -e \"TO_PORT=${toPort}\" imedvediev/api-contract:0.0.1"
echo "Execute: $dockerCmdD"
select x in "Background" "Foreground" "Cancel"; do
    case $x in
        Background ) eval ${dockerCmdD}; break;;
        Foreground ) eval ${dockerCmdF}; break;;
        Cancel ) exit;;
    esac
done
