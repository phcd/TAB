package com.archermind.txtbl.dal.business;

import com.archermind.txtbl.domain.LocationServiceResponse;
import com.archermind.txtbl.domain.Poke;

public interface IPokeService {

    /**
    * Sends an email to a Peekster
    * @param poke the poke contents to be sent on to a user
    * @param push True if the Peekster should be notified immediately.
    */
    LocationServiceResponse poke(Poke poke, boolean push);
}
