Vagrant.configure(2) do |config|
    # define a name instead of just 'default'
    config.vm.define "jmeter-smtp-server-plugin"

    config.vm.box = "generic/ubuntu1804"

    config.vm.provision "shell", inline: <<-SHELL
sudo apt-get -y update
sudo apt-get -y install git openjdk-8-jdk maven
SHELL

  config.vm.post_up_message = <<-POSTUP
Log into the VM via 'vagrant ssh'. Directory synchronized with host at /vagrant
To compile and run tests:
    cd /vagrant
    mvn install
POSTUP

end
