package cricket.merstham.website.frontend.model;

public class DataTableColumn {
    private String key;
    private String fieldName;
    private boolean sortable = true;
    private boolean display = true;

    public String getKey() {
        return key;
    }

    public DataTableColumn setKey(String key) {
        this.key = key;
        return this;
    }

    public String getFieldName() {
        return fieldName;
    }

    public DataTableColumn setFieldName(String fieldName) {
        this.fieldName = fieldName;
        return this;
    }

    public boolean isSortable() {
        return sortable;
    }

    public DataTableColumn setSortable(boolean sortable) {
        this.sortable = sortable;
        return this;
    }

    public boolean isDisplay() {
        return display;
    }

    public DataTableColumn setDisplay(boolean display) {
        this.display = display;
        return this;
    }
}
