package org.xbib.web.dispatcher;

import org.xbib.web.dispatcher.baw.BAWDispatcherSettings;
import org.xbib.web.dispatcher.bay.BAYDispatcherSettings;
import org.xbib.web.dispatcher.ber.BERDispatcherSettings;
import org.xbib.web.dispatcher.ham.HAMDispatcherSettings;
import org.xbib.web.dispatcher.hes.HESDispatcherSettings;
import org.xbib.web.dispatcher.nie.NIEDispatcherSettings;
import org.xbib.web.dispatcher.nrw.NRWDispatcherSettings;
import org.xbib.web.dispatcher.saa.SAADispatcherSettings;
import org.xbib.web.dispatcher.sax.SAXDispatcherSettings;
import org.xbib.web.dispatcher.thu.THUDispatcherSettings;

import java.util.List;
import java.util.Map;

public interface DispatcherSettings {

    public static class Factory {
        final static DispatcherSettings baw = new BAWDispatcherSettings();
        final static DispatcherSettings bay = new BAYDispatcherSettings();
        final static DispatcherSettings ber = new BERDispatcherSettings();
        final static DispatcherSettings ham = new HAMDispatcherSettings();
        final static DispatcherSettings hes = new HESDispatcherSettings();
        final static DispatcherSettings nie = new NIEDispatcherSettings();
        final static DispatcherSettings nrw = new NRWDispatcherSettings();
        final static DispatcherSettings saa = new SAADispatcherSettings();
        final static DispatcherSettings sax = new SAXDispatcherSettings();
        final static DispatcherSettings thu = new THUDispatcherSettings();
        public static DispatcherSettings getDispatcherSettings(String group) {
            switch (group) {
                case "baw" : return baw;
                case "bay" : return bay;
                case "ber" : return ber;
                case "ham" : return ham;
                case "hes" : return hes;
                case "nie" : return nie;
                case "nrw" : return nrw;
                case "saa" : return saa;
                case "sax" : return sax;
                case "thu" : return thu;
                default: return nrw;
            }
        }
    }

    List<String> getPriority();

    public Map<String,String> getServiceMap();

    public Map<String, List<String>> getServiceRestrictions();

    public  List<String> getGroups();
}
