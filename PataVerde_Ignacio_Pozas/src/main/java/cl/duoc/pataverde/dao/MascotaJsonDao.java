package cl.duoc.pataverde.dao;

import cl.duoc.pataverde.model.Gato;
import cl.duoc.pataverde.model.Perro;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class MascotaJsonDao implements MascotaDao {

    private static final String RUTA_PERROS = "perros.json";
    private static final String RUTA_GATOS = "gatos.json";
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public List<Perro> cargarPerros() {
        Path path = Path.of(RUTA_PERROS);
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }
        try (FileReader reader = new FileReader(RUTA_PERROS)) {
            Type listType = new TypeToken<ArrayList<Perro>>() {}.getType();
            List<Perro> lista = gson.fromJson(reader, listType);
            return lista != null ? lista : new ArrayList<>();
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public List<Gato> cargarGatos() {
        Path path = Path.of(RUTA_GATOS);
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }
        try (FileReader reader = new FileReader(RUTA_GATOS)) {
            Type listType = new TypeToken<ArrayList<Gato>>() {}.getType();
            List<Gato> lista = gson.fromJson(reader, listType);
            return lista != null ? lista : new ArrayList<>();
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public void guardarPerros(List<Perro> perros) {
        try (FileWriter writer = new FileWriter(RUTA_PERROS)) {
            gson.toJson(perros, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void guardarGatos(List<Gato> gatos) {
        try (FileWriter writer = new FileWriter(RUTA_GATOS)) {
            gson.toJson(gatos, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}