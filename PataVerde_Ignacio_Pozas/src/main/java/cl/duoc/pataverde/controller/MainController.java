package cl.duoc.pataverde.controller;

import cl.duoc.pataverde.dao.MascotaDao;
import cl.duoc.pataverde.dao.MascotaJsonDao;
import cl.duoc.pataverde.model.Gato;
import cl.duoc.pataverde.model.Mascota;
import cl.duoc.pataverde.model.Perro;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class MainController {

    @FXML private ComboBox<String> cmbTipo;
    @FXML private TextField txtNombre;
    @FXML private TextField txtEdad;
    @FXML private TextField txtPeso;
    @FXML private TextField txtRaza;
    @FXML private CheckBox chkVacunado;
    @FXML private CheckBox chkAdopcion;
    @FXML private CheckBox chkExterior;
    @FXML private Button btnRegistrar;
    @FXML private Button btnEliminar;
    @FXML private TextField txtBuscar;
    @FXML private Label lblMensaje;

    @FXML private TableView<Mascota> tablaMascotas;
    @FXML private TableColumn<Mascota, String> colTipo;
    @FXML private TableColumn<Mascota, String> colNombre;
    @FXML private TableColumn<Mascota, Integer> colEdad;
    @FXML private TableColumn<Mascota, Double> colPeso;
    @FXML private TableColumn<Mascota, Double> colCosto;

    private final MascotaDao dao = new MascotaJsonDao();
    private final ObservableList<Mascota> listaMascotas = FXCollections.observableArrayList();
    private FilteredList<Mascota> listaFiltrada;

    @FXML
    public void initialize() {
        // 1. Configurar ComboBox
        cmbTipo.getItems().addAll("Perro", "Gato");
        cmbTipo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> actualizarCamposSegunTipo(newVal));

        // 2. Configurar Columnas de la Tabla
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colEdad.setCellValueFactory(new PropertyValueFactory<>("edad"));
        colPeso.setCellValueFactory(new PropertyValueFactory<>("peso"));
        colCosto.setCellValueFactory(new PropertyValueFactory<>("costoConsulta"));

        // 3. Cargar Datos Persistidos
        listaMascotas.addAll(dao.cargarPerros());
        listaMascotas.addAll(dao.cargarGatos());

        // 4. Configurar Filtrado Dinámico (IE 2.5.2)
        listaFiltrada = new FilteredList<>(listaMascotas, p -> true);
        txtBuscar.textProperty().addListener((observable, oldValue, newValue) -> {
            listaFiltrada.setPredicate(mascota -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String filter = newValue.toLowerCase().trim();
                return mascota.getNombre().toLowerCase().contains(filter);
            });
        });
        tablaMascotas.setItems(listaFiltrada);

        // Estado inicial de controles
        actualizarCamposSegunTipo(null);

        // se calcula el costo de la consulta
        colCosto.setCellValueFactory(new PropertyValueFactory<>("costoConsulta"));

    }

    private void actualizarCamposSegunTipo(String tipo) {
        boolean esPerro = "Perro".equals(tipo);
        boolean esGato = "Gato".equals(tipo);

        txtRaza.setDisable(!esPerro);
        chkVacunado.setDisable(!esPerro);
        chkAdopcion.setDisable(!esPerro);

        chkExterior.setDisable(!esGato);

        if (!esPerro) {
            txtRaza.clear();
            chkVacunado.setSelected(false);
            chkAdopcion.setSelected(false);
        }
        if (!esGato) {
            chkExterior.setSelected(false);
        }
    }

    @FXML
    private void handleRegistrar() {
        String tipo = cmbTipo.getValue();
        String nombre = txtNombre.getText().trim();
        String edadTxt = txtEdad.getText().trim();
        String pesoTxt = txtPeso.getText().trim();

        if (tipo == null || tipo.isEmpty()) {
            mostrarMensaje("Debe seleccionar un tipo de mascota.", true);
            return;
        }

        int edad;
        try {
            edad = Integer.parseInt(edadTxt);
        } catch (NumberFormatException e) {
            mostrarMensaje("Edad: debe ser un número entero entre 0 y 30.", true);
            return;
        }

        double peso;
        try {
            peso = Double.parseDouble(pesoTxt);
        } catch (NumberFormatException e) {
            mostrarMensaje("Peso: debe ser un valor numérico mayor que cero.", true);
            return;
        }

        try {
            Mascota nuevaMascota;
            if ("Perro".equals(tipo)) {
                String raza = txtRaza.getText().trim();
                if (raza.isEmpty()) {
                    mostrarMensaje("Raza: no puede estar vacía.", true);
                    return;
                }
                boolean vacunado = chkVacunado.isSelected();
                boolean adopcion = chkAdopcion.isSelected();

                Perro perro = new Perro(nombre, edad, peso, raza, vacunado);
                if (adopcion) {
                    perro.marcarDisponibleParaAdopcion();
                }
                nuevaMascota = perro;
            } else {
                boolean exterior = chkExterior.isSelected();
                nuevaMascota = new Gato(nombre, edad, peso, exterior);
            }

            // Agregar a la lista y sincronizar con los archivos JSON
            listaMascotas.add(nuevaMascota);
            sincronizarPersistencia();

            mostrarMensaje(nuevaMascota.getNombre() + " (" + tipo + ") registrado correctamente.", false);
            limpiarFormulario();

        } catch (IllegalArgumentException e) {
            mostrarMensaje(e.getMessage(), true);
        }
    }

    @FXML
    private void handleEliminar() {
        Mascota seleccionada = tablaMascotas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarMensaje("Debe seleccionar una mascota de la tabla para eliminarla.", true);
            return;
        }

        listaMascotas.remove(seleccionada);
        sincronizarPersistencia();
        mostrarMensaje(seleccionada.getNombre() + " eliminado correctamente.", false);
    }

    private void sincronizarPersistencia() {
        List<Perro> perros = new ArrayList<>();
        List<Gato> gatos = new ArrayList<>();

        for (Mascota m : listaMascotas) {
            if (m instanceof Perro) {
                perros.add((Perro) m);
            } else if (m instanceof Gato) {
                gatos.add((Gato) m);
            }
        }
        dao.guardarPerros(perros);
        dao.guardarGatos(gatos);
    }

    private void limpiarFormulario() {
        cmbTipo.getSelectionModel().clearSelection();
        txtNombre.clear();
        txtEdad.clear();
        txtPeso.clear();
        txtRaza.clear();
        chkVacunado.setSelected(false);
        chkAdopcion.setSelected(false);
        chkExterior.setSelected(false);
        actualizarCamposSegunTipo(null);
    }

    private void mostrarMensaje(String texto, boolean esError) {
        lblMensaje.setText(texto);
        lblMensaje.setTextFill(esError ? Color.RED : Color.GREEN);
    }

}