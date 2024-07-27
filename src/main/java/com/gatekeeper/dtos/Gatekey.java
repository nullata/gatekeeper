package com.gatekeeper.dtos;

import java.util.Objects;

/**
 *
 * @author null
 */
public class Gatekey {

    private String gatekey;

    public Gatekey() {
    }

    public Gatekey(String gatekey) {
        this.gatekey = gatekey;
    }

    public String getGatekey() {
        return gatekey;
    }

    public void setGatekey(String gatekey) {
        this.gatekey = gatekey;
    }

    @Override
    public String toString() {
        return "Gatekey{" + "gatekey=" + gatekey + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + Objects.hashCode(this.gatekey);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Gatekey other = (Gatekey) obj;
        return Objects.equals(this.gatekey, other.gatekey);
    }

}
