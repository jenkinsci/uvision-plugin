/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jenkins.uvision;

/**
 *
 * @author prasad
 */
public class Test {
    private String file;
    private String name;
    private String coverage;
    private String instructions;
    private String id;
    private String duration;

    
    public String getId() {
        return id;
    }

    public String getDuration() {
        return duration;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCoverage(String coverage) {
        float value = Float.parseFloat(coverage);
        if(value <= 1)
        {
            value = value*100;
        }
        this.coverage = String.valueOf((int)value);
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public String getFile() {
        return file;
    }

    public String getName() {
        return name;
    }

    public String getCoverage() {
        return coverage;
    }

    public String getInstructions() {
        return instructions;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ID : ").append(getId()).append("\n");
        sb.append("File : ").append(getFile()).append("\n");
        sb.append("Name : ").append(getName()).append("\n");
        sb.append("Coverage : ").append(getCoverage()).append("\n");
        sb.append("Instructions : ").append(getInstructions()).append("\n");
        sb.append("Duration : ").append(getDuration()).append("\n");
        return sb.toString();
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
    
    
}
