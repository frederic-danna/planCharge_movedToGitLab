package fr.gouv.agriculture.dal.ct.planCharge.ihm.controller;

import fr.gouv.agriculture.dal.ct.ihm.IhmException;
import fr.gouv.agriculture.dal.ct.ihm.view.TableViews;
import fr.gouv.agriculture.dal.ct.planCharge.ihm.controller.suiviActionsUtilisateur.AjoutTache;
import fr.gouv.agriculture.dal.ct.planCharge.ihm.model.charge.PlanChargeBean;
import fr.gouv.agriculture.dal.ct.planCharge.ihm.model.tache.TacheBean;
import fr.gouv.agriculture.dal.ct.planCharge.ihm.model.tache.TotalTacheBean;
import fr.gouv.agriculture.dal.ct.planCharge.util.Exceptions;
import fr.gouv.agriculture.dal.ct.planCharge.util.Objects;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;

/**
 * Created by frederic.danna on 26/03/2017.
 *
 * @author frederic.danna
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class TachesController extends AbstractTachesController<TacheBean> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TachesController.class);

    private static TachesController instance;

    public static TachesController instance() {
        return instance;
    }

    /*
     La couche "View" :
      */

    @SuppressWarnings("NullableProblems")
    @NotNull
    @FXML
    private TableView<TacheBean> tachesTable;

    @SuppressWarnings("NullableProblems")
    @NotNull
    @FXML
    private TableView<TotalTacheBean> totauxTachesTable;

    @SuppressWarnings("NullableProblems")
    @NotNull
    @FXML
    private TableColumn<TotalTacheBean, Long> nbrTachesATraiterColumn;

    @SuppressWarnings("NullableProblems")
    @NotNull
    @FXML
    private TableColumn<TotalTacheBean, Double> totalResteAFaireColumn;

    /*
     La couche métier :
      */
    @SuppressWarnings("NullableProblems")
    @NotNull
    final // 'final' pour empêcher de resetter cette variable.
    private PlanChargeBean planChargeBean = PlanChargeBean.instance();

    // TODO FDA 2017/05 Résoudre le warning de compilation (unchecked assignement).
    @SuppressWarnings({"unchecked", "rawtypes"})
    @NotNull
    final // 'final' pour empêcher de resetter cette ObsevableList, ce qui enleverait les Listeners.
    private ObservableList<TacheBean> planificationsBeans = (ObservableList) planChargeBean.getPlanificationsBeans();


    /**
     * The constructor.
     * The constructor is called before the initialize() method.
     */
    public TachesController() throws IhmException {
        super();
        if (instance != null) {
            throw new IhmException("Instanciation à plus d'1 exemplaire.");
        }
        instance = this;
    }


    @SuppressWarnings("SuspiciousGetterSetter")
    @NotNull
    @Override
    ObservableList<TacheBean> getTachesBeans() {
        return planificationsBeans;
    }

    @NotNull
    @Override
    TableView<TacheBean> getTachesTable() {
        return tachesTable;
    }


    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    protected void initialize() throws IhmException {
        LOGGER.debug("Initialisation...");

        super.initialize();

        totauxTachesTable.getItems().setAll(new TotalTacheBean());

        tachesTable.getItems().addListener((ListChangeListener<? super TacheBean>) change -> {
            assert totauxTachesTable.getItems().size() == 1;
            TotalTacheBean totalTacheBean = totauxTachesTable.getItems().get(0);

            long nbrTachesATraiter = tachesTable.getItems().parallelStream().filter(TacheBean::estATraiter).count();
            totalTacheBean.setNbrTachesATraiter(nbrTachesATraiter);

            Double totalRAF = tachesTable.getItems().parallelStream().mapToDouble(tacheBean -> Objects.value(tacheBean.getCharge(), 0.0)).sum();
            totalTacheBean.setTotalResteAFaire(totalRAF);

        });

        nbrTachesATraiterColumn.setCellValueFactory((TableColumn.CellDataFeatures<TotalTacheBean, Long> cellData) -> cellData.getValue().nbrTachesATraiterProperty().asObject());
        totalResteAFaireColumn.setCellValueFactory((TableColumn.CellDataFeatures<TotalTacheBean, Double> cellData) -> cellData.getValue().totalResteAFaireProperty().asObject());

        LOGGER.info("Initialisé.");
    }

/* Le menu contextuel est défini dans le fichier FXML.
    void definirMenuContextuel() {
        super.definirMenuContextuel();

        if (menuVoirTacheDansPlanCharge == null) {
            menuVoirTacheDansPlanCharge = new MenuItem("Voir la tâche dans le plan de charge");
            menuVoirTacheDansPlanCharge.setOnAction(event -> afficherPlanification());
        }
        if (!tachesTableContextMenu.getItems().contains(menuVoirTacheDansPlanCharge)) {
            tachesTableContextMenu.getItems().add(menuVoirTacheDansPlanCharge);
        }

    }
*/

    // Surchargée juste pour pouvoir ajouter le @FXML.
    @FXML
    @NotNull
    protected TacheBean ajouterTache(@SuppressWarnings("unused") ActionEvent event) throws Exception {
        LOGGER.debug("ajouterTache...");
        //noinspection OverlyBroadCatchBlock
        try {
            //noinspection UnnecessarySuperQualifier
            TacheBean tacheBean = super.ajouterTache();

            planChargeBean.vientDEtreModifie();
            getSuiviActionsUtilisateur().historiser(new AjoutTache<>(tacheBean, getTachesBeans()));

            ihm.getApplicationController().majBarreEtat();
            return tacheBean;
        } catch (IhmException e) {
            throw new Exception("Impossible d'ajouter une tâche.", e);
        }
    }

    @Override
    TacheBean nouveauBean() throws IhmException {
        return ihm.getChargesController().nouveauBean();
    }

    @FXML
    private void afficherPlanification(@SuppressWarnings("unused") ActionEvent actionEvent) {
        afficherPlanification();
    }

    private void afficherPlanification() {
        TacheBean tacheBean = TableViews.selectedItem(tachesTable);
        if (tacheBean == null) {
            //noinspection HardcodedLineSeparator,MagicNumber
            ihm.afficherPopUp(
                    Alert.AlertType.ERROR,
                    "Impossible d'afficher la planification pour la tâche",
                    "Aucune tâche n'est actuellement sélectionnée."
                            + "\nSélectionnez d'abord une tâche (ligne), puis re-cliquez.",
                    400, 200
            );
            return;
        }
        try {
            ihm.getApplicationController().afficherModuleCharges();
            TableViews.focusOnItem(ihm.getChargesController().getTachesTable(), tacheBean);
        } catch (IhmException e) {
            LOGGER.error("Impossible d'afficher la planification pour la tâche " + tacheBean.getId() + ".", e);
            ihm.afficherPopUp(
                    Alert.AlertType.ERROR,
                    "Impossible d'afficher la planification pour la tâche",
                    Exceptions.causes(e)
            );
        }
    }

}
