# OSPF(Open Shortest Path First) Router

## How to compile and make the programs
- You need minimum of JDK 8
- Available command:
`make`
`make all`
`make clean`

## Router program
~~~~
Number of parameters: 4
Parameter:
      $1: <routerId> - it should be unique for each router
      $2: <nseHost> - the host where the Network State Emulator is running
      $3: <nsePort> - the port number of the Network State Emulator
      $4: <routerPort> - the router port
How to run:
      ./router $1 $2 $3 $4
~~~~

## Built and Tested Machines
1. Built and tested on two different student.cs.machines
      - ubuntu1404-004.student.uwaterloo.ca
      - ubuntu1404-008.student.uwaterloo.ca
