# -*- mode: ruby -*-
# vi: set ft=ruby :

dev_mode = 'false'

unless Vagrant.has_plugin?("vagrant-docker-compose")
  system("vagrant plugin install vagrant-docker-compose")
  puts "Dependencies installed, please try the command again."
  exit
end

# All Vagrant configuration is done below. The "2" in Vagrant.configure
# configures the configuration version (we support older styles for
# backwards compatibility). Please don't change it unless you know what
# you're doing.
Vagrant.configure(2) do |config|
  # The most common configuration options are documented and commented below.
  # For a complete reference, please see the online documentation at
  # https://docs.vagrantup.com.

  # Every Vagrant development environment requires a box. You can search for
  # boxes at https://atlas.hashicorp.com/search.
  config.vm.box = "ubuntu/trusty64"

  config.vm.hostname = "jedis-dev.spawar.navy.mil"

  #don't worry abou replacing the insecure key
  config.ssh.insert_key = false  

  # Disable automatic box update checking. If you disable this, then
  # boxes will only be checked for updates when the user runs
  # `vagrant box outdated`. This is not recommended.
  # config.vm.box_check_update = false

  # Create a forwarded port mapping which allows access to a specific port
  # within the machine from a port on the host machine. In the example below,
  # accessing "localhost:8080" will access port 80 on the guest machine.
  # config.vm.network "forwarded_port", guest: 80, host: 8080
  # Docker 
  #config.vm.network "forwarded_port", guest: 443, host: 443
  #config.vm.network "forwarded_port", guest: 10389, host: 10390


  # Create a private network, which allows host-only access to the machine
  # using a specific IP.
  # config.vm.network "private_network", ip: "192.168.33.10"
  config.vm.network "private_network", ip: "192.168.56.4"

  # Create a public network, which generally matched to bridged network.
  # Bridged networks make the machine appear as another physical device on
  # your network.
  # config.vm.network "public_network"

  # Share an additional folder to the guest VM. The first argument is
  # the path on the host to the actual folder. The second argument is
  # the path on the guest to mount the folder. And the optional third
  # argument is a set of non-required options.
  # config.vm.synced_folder "../data", "/vagrant_data"
  # don't share the normally default /vagrant folder
  config.vm.synced_folder ".", "/opt/soaf"
  config.vm.synced_folder "pki", "/etc/pki/tls"   

  # Provider-specific configuration so you can fine-tune various
  # backing providers for Vagrant. These expose provider-specific options.
  # Example for VirtualBox:
  #
  config.vm.provider "virtualbox" do |vb|
    # Display the VirtualBox GUI when booting the machine
    vb.gui = false 
    # Customize the amount of memory on the VM:
    vb.memory = "1024"
    vb.name = "soaf-dev-vm"
  end
  #
  # View the documentation for the provider you are using for more
  # information on available options.

  # Define a Vagrant Push strategy for pushing to Atlas. Other push strategies
  # such as FTP and Heroku are also available. See the documentation at
  # https://docs.vagrantup.com/v2/push/atlas.html for more information.
  # config.push.define "atlas" do |push|
  #   push.app = "YOUR_ATLAS_USERNAME/YOUR_APPLICATION_NAME"
  # end

  # Enable provisioning with a shell script. Additional provisioners such as
  # Puppet, Chef, Ansible, Salt, and Docker are also available. Please see the
  # documentation for more information about their specific syntax and use.
  # config.vm.provision "shell", inline: <<-SHELL
  #   sudo apt-get update
  #   sudo apt-get install -y apache2
  # SHELL
  # install jdk and maven
  #config.vm.provision "shell", path:"./provisioning/install-maven.sh"
  # build source
  config.vm.provision "shell", inline:"echo 'export HOSTNAME=jedis-dev.spawar.navy.mil' >> ~/.profile"
  #install docker
  config.vm.provision :docker
  #install docker-compose and start containers

  if dev_mode == 'true' 
    config.vm.provision :docker_compose, run: "always", yml: "/opt/soaf/docker-compose-dev.yml"  
  else
    config.vm.provision :docker_compose, run: "always", yml: "/opt/soaf/docker-compose.yml"  
  end

end
