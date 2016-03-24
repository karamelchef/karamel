echo $$ > %pid_file%;
sudo apt-key adv --keyserver hkp://p80.pool.sks-keyservers.net:80 --recv-keys 58118E89F3A912897C070ADBF76221572C52609D;
sudo sh -c 'echo "deb https://apt.dockerproject.org/repo ubuntu-wily main" > /etc/apt/sources.list.d/docker.list';
sudo apt-get update;
sudo apt-get -y install linux-image-extra-$(uname -r);
sudo apt-get -y install docker-engine;
sudo usermod -a -G docker $USER;
echo '%task_id%' > %succeedtasks_filepath%
