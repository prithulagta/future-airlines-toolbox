#!/bin/bash
set -e -o pipefail

# Installation of a stable version - LTS version
wget -O /etc/yum.repos.d/jenkins.repo http://pkg.jenkins-ci.org/redhat-stable/jenkins.repo
rpm --import https://jenkins-ci.org/redhat/jenkins-ci.org.key

yum update -y
yum install -y git jenkins python-pip amazon-efs-utils make rpm-build java-1.8.0 docker

pip install ansible==2.6.5

chkconfig jenkins on

chkconfig docker on

mkdir -p /mnt/{tmp,efs}

chown jenkins:jenkins -R /mnt/efs

# Setup AWSLogs client to post logs to AWS ClousWatch
export ANSIBLE_HOST_KEY_CHECKING=False
export ANSIBLE_RETRY_FILES_ENABLED="False"

cd /tmp/ansible-playbook

export ANSIBLE_ROLES_PATH="$(pwd)/roles"
export ANSIBLE_PLAYBOOK_DIR="$(pwd)"

ansible-playbook -i "localhost," /dev/stdin <<END
---
- hosts: all
  connection: local

  vars:
      - log_streams:
        - { stream: "jenkins-master/cloud-init-output", name: "{instance_id}-cloud-init-output", file: '/var/log/cloud-init-output.log'  }
        - { stream: "jenkins-master/user-data", name: "{instance_id}-user-data", file: '/var/log/user-data.log'  }

  roles:
   - awslogs
END