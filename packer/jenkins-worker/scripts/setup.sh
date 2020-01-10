#!/bin/bash
set -e -o pipefail

# Installation of a stable version - LTS version
wget -O /etc/yum.repos.d/jenkins.repo http://pkg.jenkins-ci.org/redhat-stable/jenkins.repo
rpm --import https://jenkins-ci.org/redhat/jenkins-ci.org.key

yum update -y
yum install -y python-pip

pip install ansible==2.6.5

# Install AWSLogs and jenkins worker client
export ANSIBLE_HOST_KEY_CHECKING=False
export ANSIBLE_RETRY_FILES_ENABLED="False"

cd /tmp/ansible-playbook

ansible-playbook -i "localhost," --skip-tags "runtime" jenkins-worker.yml