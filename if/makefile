RM := rm -rf

CFLAGS = -O0 -g3 -Wall -c -fmessage-length=0
CLIBS = -lm
INCS = -I"./"
EXE = exe


COMMON_SRCS = 	common/IF_common.c common/IF_config.c common/hashtable.c

COMMON_OBJS = 	common/IF_common.o common/IF_config.o common/hashtable.o

SERIALTEST_SRCS = serialtest/IF_serialtest.c
SERIALTEST_OBJS = serialtest/IF_serialtest.o

SERIALSERVER_SRCS = serialtest/IF_serialtest.c
SERIALSERVER_OBJS = serialserver/IF_serialserver.o
#
# All Target
#
all: serialtest serialserver

$(EXE):
	@echo 'Creating directory $(EXE)'
	mkdir -p $(EXE)


#
# Targets for object files
#
common/%.o: common/%.c
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C Compiler'
	gcc $(INCS) -O0 -g3 -Wall -c -o"$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '

serialserver/%.o: serialserver/%.c
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C Compiler'
	gcc $(INCS) -O0 -g3 -Wall -c -o"$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '

serialtest/%.o: serialtest/%.c
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C Compiler'
	gcc $(INCS) -O0 -g3 -Wall -c -o"$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


# Tool invocations
serialtest: $(EXE) $(COMMON_OBJS) $(SERIALTEST_OBJS)
	@echo 'Building target: $@'
	@echo 'Invoking: GCC C Linker'
	gcc  -o"$(EXE)/$@" $(COMMON_OBJS) $(SERIALTEST_OBJS) $(CLIBS)
	@echo 'Finished building target: $@'
	@echo ' '

serialserver: $(EXE) $(COMMON_OBJS) $(SERIALSERVER_OBJS)
	@echo 'Building target: $@'
	@echo 'Invoking: GCC C Linker'
	gcc  -o"$(EXE)/$@" $(COMMON_OBJS) $(SERIALSERVER_OBJS) $(CLIBS)
	@echo 'Finished building target: $@'
	@echo ' '

# Other Targets
clean:
	rm -f serialserver/*.o serialtest/*.o common/*.o $(EXE)/*
	-@echo ' '

