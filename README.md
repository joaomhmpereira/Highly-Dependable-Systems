# Highly Dependable Systems - HDS Ledger

## Instructions to execute the project and test it 'freely'
1. Start by installing all necessary dependencies. To do so, run the following command on the root directory (HDL)
    - `mvn package -DskipTests`
2. After that, compile the project code. To do so, run the following command on the root directory (HDL):
    - `mvn clean compile`
3. Then, simply spawn the desired number of processes following these instructions:
- To spawn a **Client** process:
    - On the `Client` directory, execute the following command:
        - `mvn exec:java -Dmainclass=sec.G31.client -Dexec.args="<client_id> <client_address> <client_port> <config_file>"`

- To spawn a **Server** process:
    - On the `Server` directory, execute the following command:
        - `mvn exec:java -Dmainclass=sec.G31 -Dexec.args="<server_id> <server_addres> <server_port> <fault_type> <leader_flag> <F> <config_file>"`
        - where '<fault_type>' is either **`F`** (for byzantine processes) or **`NF`** (for correct processes), '<leader_flag>' is either **`Y`** (if process is the leader) or **`N`** (otherwise), and '< F >' is the maximum number of byzantine processes tolerated (i.e. if F is set to 1, at least 3 correct processes are required in order for the system to work). 

## Instructions to run the unit tests
1. Build the project following the first two steps mentioned in the above section
2. Run the command `mvn test` in the **`Client`** directory. This will run all unit tests. In order to run a particular test use the command `mvn test -Dtest=AppClientTest#<test-name>`, where '<test_name>' is the name of the specific test you want to execute.