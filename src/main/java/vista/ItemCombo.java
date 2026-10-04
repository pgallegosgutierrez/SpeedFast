package vista;

/**
 * Elemento de un JComboBox que muestra un texto legible (por ejemplo "3 - Juan Pérez")
 * pero conserva internamente el id del registro en la base de datos.
 */
public class ItemCombo {
    private final String id;
    private final String texto;

    /**
     * @param id    id del registro en la base de datos (null para una opción como "Todos")
     * @param texto texto que verá el usuario en el combo
     */
    public ItemCombo(String id, String texto) {
        this.id = id;
        this.texto = texto;
    }

    /**
     *
     * @return el id del registro, o null si el elemento no representa un registro
     */
    public String getId() {
        return id;
    }

    /**
     * @return el texto del elemento; es lo que el JComboBox muestra en pantalla
     */
    @Override
    public String toString() {
        return texto;
    }
}
