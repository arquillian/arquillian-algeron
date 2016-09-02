package org.arquillian.pact.provider.api;

import au.com.dius.pact.model.Pact;

import java.util.ArrayList;
import java.util.List;

public class Pacts {

    private List<Pact> pacts;

    public Pacts(List<Pact> pacts) {
        this.pacts = new ArrayList<>(pacts);
    }

    public List<Pact> getPacts() {
        return pacts;
    }
}
