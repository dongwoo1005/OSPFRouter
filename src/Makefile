DOLLAR:=$$

all:
	@echo "Compiling..."
	@echo "#!/bin/bash" > router
	@echo 'java main/Router $(DOLLAR)1 $(DOLLAR)2 $(DOLLAR)3 $(DOLLAR)4' >> router
	@chmod +x router

	javac main/*.java main/model/*.java

clean:
	rm -rf *.class
	rm -rf *.log
	rm -rf router
	rm -rf main/*.class
	rm -rf main/model/*.class
