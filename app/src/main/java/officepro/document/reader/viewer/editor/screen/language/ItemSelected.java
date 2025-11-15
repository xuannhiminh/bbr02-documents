package office.pdf.document.reader.viewer.editor.screen.language;

public class ItemSelected {
    private int entry;
    private String value;
    private int description = 0;
    private int groupType = 0;
    private boolean isExpanded = false;
    private String parentGroup;

    public ItemSelected(int entry, String value) {
        this.entry = entry;
        this.value = value;
        this.groupType = 0;
    }


    public ItemSelected(int entry, String value, int isGroup) {
        this.entry = entry;
        this.value = value;
        this.groupType = isGroup;
    }



    public int getEntry() {
        return entry;
    }

    public void setEntry(int entry) {
        this.entry = entry;
    }
    public int getGroup() {
        return groupType;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }

    public String getParentGroup() {
        return parentGroup;
    }

    public ItemSelected setParentGroup(String parentGroup) {
        this.parentGroup = parentGroup;
        return this; // Trả về đối tượng hiện tại để hỗ trợ method chaining
    }
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getDescription() {
        return description;
    }

    public void setDescription(int description) {
        this.description = description;
    }
}

