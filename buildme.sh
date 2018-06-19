mkdir -p classes
javac -d classes -sourcepath src \
	./src/component/PictureFinder/Indicator.java \
	./src/component/PictureFinder/ImageIndicatorController.java \
	./src/com/example/Main.java

cp ./src/component/PictureFinder/Indicator.fxml ./src/component/PictureFinder/ImageIndicator.fxml classes/component/PictureFinder/

jar cmf Manifest.txt PictureFinder.jar -C classes .
