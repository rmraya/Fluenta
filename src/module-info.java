module fluenta {
    
    exports com.maxprograms.fluenta;
    exports com.maxprograms.fluenta.controllers;
    exports com.maxprograms.fluenta.models;
    exports com.maxprograms.fluenta.views;    
    exports com.maxprograms.fluenta.views.resources;

    opens com.maxprograms.fluenta.models to mapdb;

    requires java.base;
    requires transitive openxliff;
    requires transitive swordfish;
    requires transitive swt;
	requires java.sql;
	requires mapdb;
    requires jsoup;
}
