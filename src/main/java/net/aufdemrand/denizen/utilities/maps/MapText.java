package net.aufdemrand.denizen.utilities.maps;

public class MapText extends MapObject {

    protected String text;

    public MapText(int x, int y, String text) {
        super(x, y);
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

}
