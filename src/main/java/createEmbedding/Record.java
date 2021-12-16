package createEmbedding;

public class Record {

    private String label;
    private String type;
    private String oldname;
    private String newname;
    private String oldStmt;
    private String newStmt;

    public String getLabel() {
        return label;
    }

    public String getType() {
        return type;
    }

    public String getOldname() {
        return oldname;
    }

    public String getNewname() {
        return newname;
    }

    public String getOldStmt() {
        return oldStmt;
    }

    public String getNewStmt() {
        return newStmt;
    }

    public Record(String label, String type, String oldname, String newname, String oldStmt, String newStmt){
        this.label=label;
        this.type=type;
        this.oldname=oldname;
        this.newname=newname;
        this.oldStmt=oldStmt;
        this.newStmt=newStmt;
    }
    public boolean isEqual(Record r){
        return this.label.equals(r.getLabel())&&this.type.equals(r.getType().trim())&&this.oldname.equals(r.getOldname().trim())
                &&this.newname.equals(r.getNewname().trim())&&this.oldStmt.equals(r.getOldStmt().trim())&&this.newStmt.equals(r.getNewStmt().trim());
    }
}
