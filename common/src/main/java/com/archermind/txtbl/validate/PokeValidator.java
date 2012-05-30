package com.archermind.txtbl.validate;

import com.archermind.txtbl.domain.LocationServiceResponse;
import com.archermind.txtbl.domain.Poke;

import com.archermind.txtbl.utils.StringUtils;
import org.jboss.logging.Logger;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

public class PokeValidator {

    private static final Logger logger = Logger.getLogger(PokeValidator.class);

    private static Poke workingPoke = null;

    private static Set<String> validTypes = new HashSet<String>();

    static{
        validTypes.add("SMS");
        validTypes.add("FB");
        validTypes.add("EMAIL");
        validTypes.add("APP");
    }


    private static Poke rejectEmpty(){
        LocationServiceResponse response = new LocationServiceResponse();
        response.setCode(LocationServiceResponse.INVALID_REQUEST_ERROR);
        logger.error(response.getMessage());
        workingPoke.setResponse(response);
        return workingPoke;


    }

    public static Poke validate(Poke poke){

        workingPoke = poke;

        LocationServiceResponse response = new LocationServiceResponse();

        if(StringUtils.isEmpty(poke.getSentDateUTC())) return rejectEmpty();
        if(StringUtils.isEmpty(poke.getTo())) return rejectEmpty();
        if(StringUtils.isEmpty(poke.getFrom())) return rejectEmpty();
        if(StringUtils.isEmpty(poke.getAlias())) poke.setAlias(poke.getFrom());
        if(StringUtils.isEmpty(poke.getSubject())) poke.setSubject("(No Subject)");
        if(StringUtils.isEmpty(poke.getType())) poke.setType("APP");
        if (!(validTypes.contains(poke.getType().toUpperCase()))){
            poke.setType("APP");
        }


        try {
            // TODO - this only should be done if "from" is not a 10-digit (phone) number
            // make sure "from" is a valid email address - interestingly enough, no exception is thrown
            // for email address "test". If we detect that case, we append "@" to force a failure
//                    if (from != null && from.indexOf('@') < 0) {
//                        from = from + "@";
//                    }
            new InternetAddress(poke.getFrom());

            // TODO - this only should be done if "to" is not a 10-digit (phone) number
            // make sure "to" is a valid email address - interestingly enough, no exception is thrown
            // for email address "test". If we detect that case, we append "@" to force a failure
//                    if (to != null && to.indexOf('@') < 0) {
//                        to = to + "@";
//                    }
            new InternetAddress(poke.getTo());


            // Mail Date in peeklocationormap
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date mailDate = sdf.parse(poke.getSentDateUTC());
            poke.setMailDate(mailDate);

        } catch (ParseException e) {
            logger.error("Unable to process poke request. Cannot parse mail date from " + poke.getSentDateUTC());
            response = new LocationServiceResponse();
            response.setCode(LocationServiceResponse.DATE_FORMAT_ERROR);

        } catch (AddressException e) {
            logger.error("Unable to process poke request: " + e.getMessage());
            response = new LocationServiceResponse();
            response.setCode(LocationServiceResponse.INVALID_EMAIL_ERROR);
        }

        poke.setResponse(response);
        return poke;

}

}
