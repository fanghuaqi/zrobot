CROSS_COMPILE_PATH=/opt/toolschain/arm-xilinxA9/bin/
CROSS_COMPILE=arm-xilinx-linux-gnueabi-
INSTALL_PATH=
main:
	$(CROSS_COMPILE)gcc err*.c dae*.c smart_car_move.c zynq_server.c -o zynq_server_arm.o
clean:
	rm -rf ./*.o
install:
	cp ./*.o $(INSTALL_PATH)

