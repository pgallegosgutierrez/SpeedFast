![Duoc UC](https://www.duoc.cl/wp-content/uploads/2022/09/logo-0.png)


## 👤 Autor del proyecto
- **Nombre completo:** Paula Gallegos Gutiérrez
- **Sección:** 002A
- **Carrera:** Analista Programador Computacional
- **Sede:** Online

## SpeedFast - Sistema de Gestión de Entregas

Proyecto académico en Java para la asignatura Desarrollo Orientado a Objetos II (Duoc UC). Modela el sistema de pedidos y entregas de la empresa ficticia SpeedFast, con una interfaz gráfica Swing y persistencia en MySQL mediante JDBC.

## ✅ Funcionalidades

- **Gestión de repartidores:** registrar, editar, eliminar y listar en tabla.
- **Gestión de pedidos:** registrar (dirección, tipo y estado), editar, eliminar y listar en tabla, con filtros por tipo y por estado.
- **Gestión de entregas:** registrar una entrega asociando un pedido y un repartidor, con fecha y hora; editar, eliminar y listar en tabla, con filtros por pedido y por repartidor.
- Las tablas se actualizan después de cada operación, y las listas de pedidos y repartidores se refrescan solas cuando esos datos cambian.
- Los datos se validan antes de guardarlos y los errores de la base de datos se muestran con mensajes claros.

## 🛠️ Requisitos

- JDK 17 o superior.
- MySQL 8.
- IntelliJ IDEA. El conector de MySQL (`mysql-connector-j`) se descarga solo con Maven, a partir del `pom.xml`.

## 🗄️ Base de datos

El script `sql/speedfast_db.sql` crea la base de datos `speedfast_db` con las tablas `repartidores`, `pedidos` y `entregas`. Se puede ejecutar en MySQL Workbench o desde la consola:

```
mysql -u root -p < sql/speedfast_db.sql
```

Si MySQL corre en Docker (contenedor llamado `mysql`):

```
docker exec -i mysql mysql -uroot -pmysqlpass < sql/speedfast_db.sql
```

La aplicación se conecta a `127.0.0.1:3306` con el usuario `root` y la contraseña `mysqlpass`. Si tu MySQL usa otros datos, cámbialos en `src/main/java/database/ConexionDB.java`.

## ▶️ Cómo ejecutar

1. Abrir la carpeta del proyecto en IntelliJ IDEA (se reconoce como proyecto Maven).
2. Crear la base de datos con el script, como se indica arriba.
3. Ejecutar la clase `main.Main`.

## 🧱 Estructura general del proyecto
```Organización de Packages
📁 sql/           # Script que crea la base de datos speedfast_db.
📁 src/main/java/
├── main/        # Punto de entrada de la aplicación (Main).
├── model/       # Lógica del dominio: pedidos, estados, repartidores y entregas.
├── database/    # Capa DAO: conexión a MySQL (ConexionDB) y un DAO por entidad con create, readAll, update y delete.
├── vista/       # Ventanas Swing. Muestran y capturan datos usando las clases del modelo y los DAO.
├── util/        # Clases utilitarias. Datos de ejemplo para pruebas. No forma parte del modelo.

````
