
# Overview
The following instructions are for people who already have a linux based instance which has at least 16GB of memory running. If you still need to do this you can do so in any way you are comfortable, or follow our [AWS](./create_vm_in_aws.md) or [Azure](./create_vm_in_azure.md) instructions.

# Assumptions:
1) You have followed the instructions in 'create vm in aws' and have at least 16GB of Memory
1) You are logged into your AWS instance. (Not your local machine)
2) You have a honeycomb Key (If you don't please see one of the workshop owners)
3) Your user id is ubuntu. (You can check this by using the command `whoami`. If this is not the case where you see ubuntu in the instructions below replace with your user id)
4) You don't have root access and will be using sudo

## Startup your AWS instance from your local machine and SSH in:
Check the name of your running machine:
``` bash
docker-machine ls
eval $(docker-machine env o11y-workshop)
```
SSH into your running machine:
``` bash
docker-machine ssh o11y-workshop
```

## Sudo
setup sudo so you don't have to use it at each command
``` bash
sudo -i
```

## Install docker
Depending on how you built your machine you may already have Docker installed. If you run `docker --version` and get back something like `Docker version 18.06.1-ce, build e68fc7a` you are all set and can continue to the [next section](#increase-max-map-count-for-elastic-search). If you instead get something like `command not found: docker` please contintue with this list of instructions.

1) Get your instance up to date
``` bash
 apt-get update
```
2) Install some stuff need for the workshop
``` bash
apt-get install -y \
    apt-transport-https \
    ca-certificates \
    curl \
    gnupg-agent \
    software-properties-common
```
3) Download Docker gpg
``` bash
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -
```
4) Download the latest docker instance for ubuntu
``` bash
add-apt-repository \
   "deb [arch=amd64] https://download.docker.com/linux/ubuntu \
   $(lsb_release -cs) \
   stable"
```
4) Refresh and update to make sure its all latest everything
``` bash
apt-get update
```
4a) Install Docker
``` bash
apt-get install -y docker-ce docker-ce-cli containerd.io
```
5) Download docker compose
``` bash
curl -L "https://github.com/docker/compose/releases/download/1.24.1/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
```
6)  Change the directory permissions
``` bash
chmod +x /usr/local/bin/docker-compose
```

## increase max map count for elastic search
``` bash
sysctl -w vm.max_map_count=262144
```

## set HONEYCOMB enviornment variable
``` bash
export HONEYCOMB_KEY=<ask workshop owners for this>
```
## checkout repo
``` bash
git clone https://github.com/feature-creeps/observability-workshop.git $HOME/observability-workshop
```
## install and start stack
``` bash
$HOME/observability-workshop/start-stack-in-level.sh 9
```
 Note - you will see warnings go by, and at the very end, there's an error "dataset not found", don't worry, it was still successful.

## Check Stack is working
1) First find your IP address
``` bash
dig +short myip.opendns.com @resolver1.opendns.com
```
2) Put ip address into your browser, this should show the DIMA website
3) Check all the telemetry stuff (see the ports below)

#### Telemetry infrastructure components (`stack/infrastructure`)

| dir                         | desc                                      |Running port |
| ---                         | ---                                       | --          |
| grafana/                    | time series visualizer                    | 3000        |
| kibana/                     | time series visualizer                    | 5601        |
| prometheus/                 | time series data base                     | 9090        |
| zipkin                      | tracing (in the docker-compose files)     | 9411        |


## Stopping the Stack
>Note replace ubuntu with your user id if its not ubuntu. To find out what it is type whoami
``` bash
docker-compose --project-directory $HOME/observability-workshop/stack/compose/ -f $HOME/observability-workshop/stack/compose/docker-compose-level-9.yml down -v --remove-orphans
```

## Restarting the Stack
``` bash
docker-compose --project-directory $HOME/observability-workshop/stack/compose/ -f $HOME/observability-workshop/stack/compose/docker-compose-level-9.yml up --build -d
```

## Removing AWS Instance
1) If you get stuck, sometimes the easiest thing to do remove what you have and begin from fresh. Into your instance type: `exit`
2) Once you are on your local machine type: `docker-machine rm o11y-workshop`
