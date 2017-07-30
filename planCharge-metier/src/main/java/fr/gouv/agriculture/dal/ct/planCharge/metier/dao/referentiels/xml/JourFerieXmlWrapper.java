package fr.gouv.agriculture.dal.ct.planCharge.metier.dao.referentiels.xml;

import fr.gouv.agriculture.dal.ct.planCharge.metier.modele.referentiels.JourFerie;
import fr.gouv.agriculture.dal.ct.planCharge.util.Dates;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.time.LocalDate;
import java.util.Date;

/**
 * Created by frederic.danna on 30/04/2017.
 */
public class JourFerieXmlWrapper {

    private Date date;
    private String description;

    /**
     * Constructeur vide (appelé notamment par JAXB).
     *
     * @return
     */
    public JourFerieXmlWrapper() {
        super();
    }

    public JourFerieXmlWrapper init(JourFerie jourFerie) {
        this.date = Dates.asDate(jourFerie.getDate());
        this.description = jourFerie.getDescription();
        return this;
    }

    @XmlAttribute(name="date", required = true)
    public Date getDate() {
        return date;
    }

    @XmlElement(name="description", required = true)
    @NotNull
    public String getDescription() {
        return description;
    }

    public void setDate(@NotNull Date date) {
        this.date = date;
    }

    public void setDescription(@NotNull String description) {
        this.description = description;
    }


    public JourFerie extract() {
        return new JourFerie(Dates.asLocalDate(date), description);
    }
}