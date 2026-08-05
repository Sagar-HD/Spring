package com.sagar.entity;

public class AnimeCharacter {
    private String name;
    private boolean hasDevilFruit;
    private boolean isLuffyCrew;
    private String specialSkill;

    public boolean isHasDevilFruit() {
        return hasDevilFruit;
    }

    public void setHasDevilFruit(boolean hasDevilFruit) {
        this.hasDevilFruit = hasDevilFruit;
    }

    public boolean isLuffyCrew() {
        return isLuffyCrew;
    }

    public void setLuffyCrew(boolean luffyCrew) {
        isLuffyCrew = luffyCrew;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpecialSkill() {
        return specialSkill;
    }

    public void setSpecialSkill(String specialSkill) {
        this.specialSkill = specialSkill;
    }
}
