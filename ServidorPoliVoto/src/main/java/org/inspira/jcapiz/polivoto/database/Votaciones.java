package org.inspira.jcapiz.polivoto.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import org.inspira.jcapiz.polivoto.actividades.ConfiguraParticipantes;
import org.inspira.jcapiz.polivoto.pojo.Categoria;
import org.inspira.jcapiz.polivoto.pojo.DatosAgenteDeInteraccion;
import org.inspira.jcapiz.polivoto.pojo.Escuela;
import org.inspira.jcapiz.polivoto.pojo.Opcion;
import org.inspira.jcapiz.polivoto.pojo.Pregunta;
import org.inspira.jcapiz.polivoto.pojo.Votacion;
import org.inspira.jcapiz.polivoto.proveedores.ProveedorDeRecursos;
import org.inspira.jcapiz.polivoto.seguridad.Hasher;
import org.inspira.jcapiz.polivoto.seguridad.MD5Hash;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Votaciones extends SQLiteOpenHelper{

    private String FILE_NAME = Environment.getExternalStorageDirectory().getAbsolutePath();
	private Context ctx;
	public Votaciones(Context context){
        super(context, "PoliVoto Electrónico", null, 1);
		ctx = context;
        // Debe existir una forma de ayudar al usuario a localizar el fichero que contenga
        // la matrícula que deba usarse para llevar a cabo la auscultación.
        FILE_NAME = FILE_NAME.concat("/"+ PreferenceManager.getDefaultSharedPreferences(ctx).getString(ConfiguraParticipantes.NOMBRE_ARCHIVO_MATRICULA_KEY, "/chu"));
	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
		if (!db.isReadOnly()) {
			// Enable foreign key constraints
			db.execSQL("PRAGMA foreign_keys=ON;");
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        /*
        db.execSQL("drop table Votacion_Replicada");
        db.execSQL("drop table DatosDeAgenteDeVotacion");
        db.execSQL("drop table Voto");
        db.execSQL("drop table Participante_Pregunta");
        db.execSQL("drop table ActiveUsers");
        db.execSQL("drop table UserActions");
        db.execSQL("drop table AttemptSucceded");
        db.execSQL("drop table LoginAttempt");
        db.execSQL("drop table Usuario");
        db.execSQL("drop table Opcion");
        db.execSQL("drop table Pregunta");
        db.execSQL("drop table VotacionGlobal");
        db.execSQL("drop table Votacion");
        db.execSQL("drop table NombreParticipante");
        db.execSQL("drop table Participante");
        db.execSQL("drop table Perfil");
        db.execSQL("drop table Escuela");
        db.execSQL("drop table Categoria");
        */
	}

	@Override
	public void onCreate(SQLiteDatabase dataBase) {

        dataBase.execSQL("create table Categoria(idCategoria INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, nombre TEXT NOT NULL)");
        dataBase.execSQL("create table Escuela(idEscuela INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, Nombre TEXT NOT NULL, idCategoria INTEGER NOT NULL, FOREIGN KEY (idCategoria) REFERENCES Categoria (idCategoria) ON DELETE CASCADE ON UPDATE NO ACTION)");
        dataBase.execSQL("create table Perfil(idPerfil INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, perfil text not null, idPerfilGlobal INTEGER DEFAULT -1)");
        dataBase.execSQL("create table Participante(Boleta TEXT, idPerfil INTEGER NOT NULL, idEscuela INTEGER NOT NULL, Fecha_Registro TEXT, BoletaHash TEXT not null, PRIMARY KEY(Boleta), foreign key(idPerfil) references Perfil(idPerfil) ON UPDATE CASCADE, foreign key(idEscuela) references Escuela(idEscuela) ON DELETE CASCADE ON UPDATE CASCADE);");
        dataBase.execSQL("create table NombreParticipante(Boleta TEXT NOT NULL, Nombre TEXT NOT NULL, ApPaterno TEXT NOT NULL, ApMaterno TEXT NOT NULL, primary key(Boleta), foreign key(Boleta) references Participante(Boleta) ON DELETE CASCADE ON UPDATE CASCADE)");

        dataBase.execSQL("create table Votacion(idVotacion INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, idEscuela INTEGER NOT NULL, Titulo text not null, Fecha_Inicio long NOT NULL, Fecha_Fin long NOT NULL, Soy_Propietario INTEGER DEFAULT 0, concluida INTEGER DEFAULT 0, FOREIGN KEY(idEscuela) REFERENCES Escuela(idEscuela) ON DELETE NO ACTION ON UPDATE NO ACTION)");
        dataBase.execSQL("create table HashVotacion(idHashVotacion INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, idVotacion INTEGER NOT NULL, Hash TEXT NOT NULL, FOREIGN KEY(idVotacion) REFERENCES Votacion(idVotacion) ON DELETE NO ACTION ON UPDATE NO ACTION)");
		dataBase.execSQL("create table VotacionGlobal(idVotacionGlobal INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, Sincronizado INTEGER DEFAULT 0, idVotacion INTEGER NOT NULL, FOREIGN KEY(idVotacion) REFERENCES Votacion(idVotacion) ON DELETE CASCADE ON UPDATE CASCADE)");
		// Debo preguntar acerca de tener un idPregunta como entero. ¿Podría sólo dejar como pk a Pregunta y hacer que idVotacion forme parte de la pk? (Relación identificadora)
        dataBase.execSQL("create table Pregunta(idPregunta INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, Pregunta TEXT not null, idVotacion INTEGER NOT NULL, FOREIGN KEY(idVotacion) REFERENCES Votacion(idVotacion))");
		dataBase.execSQL("create table Opcion(idOpcion INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, Reactivo TEXT not null, idPregunta INTEGER NOT NULL, foreign key(idPregunta) references Pregunta(idPregunta))");

		dataBase.execSQL("create table Usuario(idUsuario INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, Name text not null, Psswd blob not null)");
		dataBase.execSQL("create table LoginAttempt(idLoginAttempt INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, idUsuario INTEGER NOT NULL, Attempt_Timestamp text not null, Host text not null, foreign key(idUsuario) references Usuario(idUsuario))");
		dataBase.execSQL("create table AttemptSucceded(idLoginAttempt INTEGER not null, secretKey BLOB not null, expiration_time long not null, primary key(idLoginAttempt), foreign key(idLoginAttempt) references LoginAttempt(idLoginAttempt) ON DELETE CASCADE ON UPDATE CASCADE)");
		dataBase.execSQL("create table UserAction(idUserAction INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, idLoginAttempt INTEGER NOT NULL, Action text not null, Action_Timestamp text not null, foreign key(idLoginAttempt) references AttemptSucceded(idLoginAttempt) ON DELETE CASCADE ON UPDATE CASCADE)");
        dataBase.execSQL("create table ActiveUsers(idLoginAttempt INTEGER NOT NULL, Action text not null, Action_Timestamp text not null, primary key(idLoginAttempt), foreign key(idLoginAttempt) references AttemptSucceded(idLoginAttempt) ON DELETE CASCADE ON UPDATE CASCADE)");

        // Recuerda que el "null hack" son tres guiones. Sólo insertas registros de quienes son capturados al momento de validación.
		dataBase.execSQL("create table Participante_Pregunta(Boleta TEXT not null, idPregunta INTEGER NOT NULL, Hora_Registro text not null, Hora_Participacion text, PRIMARY KEY(Boleta,idPregunta), FOREIGN KEY(Boleta) REFERENCES Participante(Boleta) ON DELETE CASCADE ON UPDATE CASCADE, FOREIGN KEY(idPregunta) REFERENCES Pregunta(idPregunta) ON DELETE CASCADE ON UPDATE CASCADE)");
		dataBase.execSQL("create table Voto(" +
                "idVoto blob not null, " +
                "idVotacion INTEGER NOT NULL, " +
                "idPerfil INTEGER NOT NULL, " +
                "Voto text not null, " +
                "idLoginAttempt INTEGER NOT NULL, " +
                "idPregunta INTEGER NOT NULL, " +
                "primary key(idVoto), " +
                "FOREIGN KEY(idVotacion) REFERENCES Votacion(idVotacion) ON DELETE CASCADE ON UPDATE CASCADE, " +
                "foreign key(idLoginAttempt) references AttemptSucceded(idLoginAttempt) ON DELETE CASCADE ON UPDATE CASCADE, " +
                "foreign key(idPregunta) references Pregunta(idPregunta) ON DELETE CASCADE ON UPDATE CASCADE)");

        dataBase.execSQL("create table DatosDeAgenteDeVotacion(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, MainHost TEXT NOT NULL, SecondaryHost TEXT NOT NULL, HBPeriod INTEGER DEFAULT 30, Participantes TEXT NOT NULL, idVotacion INTEGER NOT NULL, FOREIGN KEY(idVotacion) REFERENCES Votacion(idVotacion))");
        // Vista que sirve para tener a la mano las preguntas totales de cada votación.
        dataBase.execSQL("create view if not exists Preguntas_Votacion as select idVotacion,count(*) as Preguntas from Pregunta group by idVotacion");

        dataBase.execSQL("create table Votacion_Replicada(" +
                "idVotacion integer not null primary key," +
                "foreign key(idVotacion) references Votacion(idVotacion) on delete cascade on update cascade" +
                ")");
        initTables(dataBase);
    }

    public void initTables(SQLiteDatabase dataBase){
        ContentValues cValues;
        String arg;
        String aux;
        String[] auxArray;
        String[] values = {
                "ÁREA CENTRAL",
                "ESCUELAS NIVEL MEDIO SUPERIOR",
                "ESCUELAS NIVEL SUPERIOR",
                "LENGUAS EXTRANJERAS",
                "CENTROS DE EDUCACIÓN CONTINUA",
                "CENTROS DE INVESTIGACIÓN Y POSGRADOS",
                "SECCIÓN DE ESTUDIOS DE POSGRADO E INVESTIGACIÓN"};
        for(int i=0; i<values.length; i++){
            Log.d("init", "Going row " + (i+1));
            cValues = new ContentValues();
            cValues.put("nombre", values[i]);
            dataBase.insert("Categoria", "---", cValues);
        }
        cValues = new ContentValues();
        cValues.put("perfil", "NaN");
        dataBase.insert("Perfil", "---", cValues);
        arg = "(1,'Centro de Difusión de Ciencia y Tecnología',1),(2,'Centro de Incubación de Empresas de Base " +
                "Tecnológica',1),(3,'Centro Nacional de Cálculo',1),(4,'Coordinación de Centros de Desarrollo " +
                "Infantil',1),(5,'Coordinación de Cooperación Académica',1),(6,'Coordinación General de Servic" +
                "ios Informáticos',1),(7,'Dirección de Administración Escolar',1),(8,'Dirección de Capital Hum" +
                "ano',1),(9,'Dirección de Desarrollo y Fomento Deportivo',1),(10,'Dirección de Educación Conti" +
                "nua',1),(11,'Dirección de Educación Superior',1),(12,'Dirección de Investigación',1),(13,'Dir" +
                "ección de Publicaciones',1),(14,'Dirección de Recursos Materiales y Servicios',1),(15,'Estaci" +
                "ón de Televisión XEIPN Canal Once',1),(16,'Órgano Interno de Control',1),(17,'Presidencia del " +
                "Decanato',1),(18,'Secretaría de Administr\u200Bación',1),(19,'Secretaría de Gestión Estratégica" +
                "',1),(20,'Secretaría de S\u200Bervicios Educativos',1),(21,'Unidad Politécnica de Gestión con P" +
                "erspectiva de Género',1),(22,'Unidad Politécnica para la Educación Virtual',1),(23,'Coordinaci" +
                "ón General de Formación en Innovación Educativa',1),(24,'Centro de Investigación y de Estudios" +
                " Avanzados',1),(25,'Comisión de Operación y Fomento de Actividades Académicas',1),(26,'Coordin" +
                "ación de Comunicación Social',1),(27,'Coordinación del Sistema Institucional de Información',1)" +
                ",(28,'Defensoría de los Derechos Politécnicos',1),(29,'Dirección de Bibliotecas',1),(30,'Direcc" +
                "ión de Cómputo y Comunicaciones',1),(31,'Dirección de Difusión y Fomento a la Cultura',1),(32,'" +
                "Dirección de Educación Media Superior',1),(33,'Dirección de Egresados y Servicio Social',1),(34" +
                ",'Dirección de Posgrado',1),(35,'Dirección de Recursos Financieros',1),(36,'Dirección de Servic" +
                "ios Estudiantiles',1),(37,'Oficina del Abogado General',1),(38,'Patronato de Obras e Instalaci" +
                "ones del IPN',1),(39,'Secretaría Académica',1),(40,'Secretaría de Extensión e Integración Soci" +
                "al',1),(41,'Secretaría de Investigación y Posgrado',1),(42,'Secretaría General',1),(43,'Unidad" +
                " Politécnica para el Desarrollo y la Competitividad Empresarial',1),(44,'Centro de Estudios Ci" +
                "entíficos y Tecnológicos No. 1 \"Gonzalo Vázquez Vela\" CECYT1',2),(45,'Centro de Estudios Cie" +
                "ntíficos y Tecnológicos No. 2 \"Miguel Bernard\" CECYT2',2),(46,'Centro de Estudios Científico" +
                "s y Tecnológicos No. 3 \"Estanislao Ramírez Ruiz\" CECYT3',2),(47,'Centro de Estudios Científi" +
                "cos y Tecnológicos No. 4 \"Lázaro Cárdenas\" CECYT4',2),(48,'Centro de Estudios Científicos y " +
                "Tecnológicos No. 5 \"Benito Juárez García\" CECYT5',2),(49,'Centro de Estudios Científicos y " +
                "Tecnológicos No. 6 \"Miguel Othón de Mendizábal\" CECYT6',2),(50,'Centro de Estudios Científic" +
                "os y Tecnológicos No. 7 \"Cuauhtémoc\" CECYT7',2),(51,'Centro de Estudios Científicos y Tecnol" +
                "ógicos No. 8 \"Narciso Bassols García\" CECYT8',2),(52,'Centro de Estudios Científicos y Tecnol" +
                "ógicos No. 9 \"Juan de Dios Bátiz\" CECYT9',2),(53,'Centro de Estudios Científicos y Tecnológic" +
                "os No. 10 \"Carlos Vallejo Márquez\" CECYT10',2),(54,'Centro de Estudios Científicos y Tecnológ" +
                "icos No. 11 \"Wilfrido Massieu Pérez\" CECYT11',2),(55,'Centro de Estudios Científicos y Tecnol" +
                "ógicos No. 12 \"José María Morelos y Pavón\" CECYT12',2),(56,'Centro de Estudios Científicos y" +
                " Tecnológicos No. 13 \"Ricardo Flores Magón\" CECYT13',2),(57,'Centro de Estudios Científicos " +
                "y Tecnológicos No. 14 \"Luis Enrique Erro\" CECYT14',2),(58,'Centro de Estudios Científicos y " +
                "Tecnológicos No. 15 \"Diódoro Antúnez Echegaray\" CECYT15',2),(59,'Centro de Estudios Tecnoló" +
                "gicos No.1 \"Walter Cross Buchanan\" CET1',2),(60,'Centro Interdisciplinario de Ciencias de la " +
                "Salud Unidad Milpa Alta (CICS)',3),(61,'Centro Interdisciplinario de Ciencias de la Salud Unidad " +
                "Santo Tomás (CICS)',3),(62,'Escuela Nacional de Ciencias Biológicas Unidad Santo Tomás (ENCB)',3),(6" +
                "3,'Escuela Nacional de Ciencias Biológicas Unidad Zacatenco (ENCB)',3),(64,'Escuela Nacional de Med" +
                "icina y Homeopatía (ENMH)',3),(65,'Escuela Superior de Comercio y Administración Unidad Santo Tomás" +
                " (ESCA)',3),(66,'Escuela Superior de Comercio y Administración Unidad Tepepan (ESCA)',3),(67,'Escuela " +
                "Superior de Cómputo (ESCOM)',3),(68,'Escuela Superior de Economía (ESE)',3),(69,'Escuela Superior De En" +
                "fermería Y Obstetricia (ESEO)',3),(70,'Escuela Superior de Física y Matemáticas (ESFM)',3),(71" +
                ",'Escuela Superior de Ingeniería Mecánica y Eléctrica Unidad Azcapotzalco (ESIME)',3),(72,'Escuel" +
                "a Superior de Ingeniería Mecánica y Eléctrica Unidad Culhuacan (ESIME)',3),(73,'" +
                "Escuela Superior de Ingeniería Mecánica y Eléctrica Unidad Ticomán (ESIME)',3),(74," +
                "'Escuela Superior de Ingeniería Mecánica y Eléctrica Unidad Zacatenco (ESIME)',3),(75," +
                "'Escuela Superior de Ingeniería Química e Industrias Extractivas (ESIQIE)',3),(76,'" +
                "Escuela Superior de Ingeniería Textil (ESIT)',3),(77,'Escuela Superior de Ingeniería y" +
                " Arquitectura Unidad Tecamachalco (ESIA)',3),(78,'Escuela Superior de Ingeniería y " +
                "Arquitectura Unidad Ticomán (ESIA)',3),(79,'Escuela Superior de Ingeniería y Arquitect" +
                "ura Unidad Zacatenco (ESIA)',3),(80,'Escuela Superior de Medicina (ESM)',3),(81,'Escuela " +
                "Superior de Turismo (EST)',3),(82,'Unidad Profesional Interdisciplinaria de Biotecnología (U" +
                "PIBI) ',3),(83,'Unidad Profesional Interdisciplinaria de Ingeniería Campus Zacatecas " +
                "(UPIIZ)',3),(84,'Unidad Profesional Interdisciplinaria de Ingeniería y Ciencias Sociales" +
                " y Administrativas (UPIICSA)',3),(85,'Unidad Profesional Interdisciplinaria en Ingeniería C" +
                "ampus Guanajuato (UPIIG)',3),(86,'Unidad Profesional Interdisciplinaria en Ingeniería y Tecnol" +
                "ogías Avanzadas (UPIITA) ',3),(87,'Dirección de Formación de Lenguas Extranjeras',4),(88,'Centro " +
                "de Lenguas Extranjeras Unidad Zacatenco',4),(89,'Centro de Lenguas Extranjeras Unidad Santo Tomás '," +
                "4),(90,'Centro de Educación Continua Unidad Allende',5),(91,'Centro de Educación " +
                "Continua Unidad Campeche',5),(92,'Centro de Educación Continua Unidad Cancún ',5),(9" +
                "3,'Centro de Educación Continua Unidad Culiacán',5),(94,'Centro de Educación Continua U" +
                "nidad Mochis',5),(95,'Centro de Educación Continua Unidad Mazatlán',5),(96,'Centro de Educ" +
                "ación Continua Unidad Oaxaca',5),(97,'Centro de Educación Continua Unidad Morelia',5),(98,'Ce" +
                "ntro de Educación Continua Unidad Tampico',5),(99,'Centro de Educación Continua Unidad Reynosa'," +
                "5),(100,'Centro de Educación Continua Unidad Tlaxcala',5),(101,'Centro de Educación Continu" +
                "a Unidad Tijuana',5),(102,'Centro de Biotecnología Genómica',6),(103,'Centro de Desarrollo " +
                "de Productos Bióticos',6),(104,'Centro de Innovación y Desarrollo Tecnológico en Cómputo',6" +
                "),(105,'Centro de Investigación e Innovación Tecnológica Azcapotzalco',6),(106,'Centro de " +
                "Investigación en Biotecnología Aplicada IPN Tlaxcala',6),(107,'Centro de Investigación en " +
                "Ciencia Aplicada y Tecnología Avanzada Unidad Altamira',6),(108,'Centro de Investigación e" +
                "n Ciencia Aplicada y Tecnología Avanzada Unidad Legaria',6),(109,'Centro de Investigación " +
                "en Ciencia Aplicada y Tecnología Avanzada Unidad Querétaro',6),(110,'Centro de Investigaci" +
                "ón en Computación',6),(111,'Centro de Investigación y Desarrollo de Tecnología Digital Tij" +
                "uana',6),(112,'Centro de Investigaciones Económicas Administrativas y Sociales',6),(113,'C" +
                "entro de Nanociencias Micro y Nanotecnologías',6),(114,'Centro Interdisciplinario de Cienc" +
                "ias Marinas',6),(115,'Centro Interdisciplinario de Investigación para el Desarrollo Integr" +
                "al Regional Unidad Durango',6),(116,'Centro Interdisciplinario de Investigación para el De" +
                "sarrollo Integral Regional Unidad Michoacán',6),(117,'Centro Interdisciplinario de Investi" +
                "gación para el Desarrollo Integral Regional Unidad Oaxaca',6),(118,'Centro Interdisciplina" +
                "rio de Investigación para el Desarrollo Integral Regional Unidad Sinaloa Guasave',6),(119," +
                "'Centro Interdisciplinario de Investigación y Estudios Sobre Medio Ambiente y Desarrollo'," +
                "6),(120,'Centro Mexicano para la Producción más Limpia',6),(121,'TechnoPoli',6),(122,'SEPI" +
                " Escuela Nacional de Ciencias Biológicas',7),(123,'SEPI Escuela Nacional de Medicina y Hom" +
                "eopatía',7),(124,'SEPI Escuela Superior de Comercio y Administración Unidad Santo Tomás',7" +
                "),(125,'SEPI Escuela Superior de Comercio y Administración Unidad Tepepan',7),(126,'SEPI E" +
                "scuela Superior de Cómputo',7),(127,'SEPI Escuela Superior de Economía',7),(128,'SEPI Escu" +
                "ela Superior de Físico y Matemáticas',7),(129,'SEPI Escuela Superior de Ingeniería Mecánic" +
                "a y Eléctrica Unidad Azcapotzalco',7),(130,'SEPI Escuela Superior de Ingeniería Mecánica y" +
                " Eléctrica Unidad Culhuacan',7),(131,'SEPI Escuela Superior de Ingeniería Mecánica y Eléct" +
                "rica Unidad Ticomán',7),(132,'SEPI Escuela Superior de Ingeniería Mecánica y Eléctrica Uni" +
                "dad Zacatenco',7),(133,'SEPI Escuela Superior de Ingeniería Química e Industrias Extractiv" +
                "as',7),(134,'SEPI Escuela Superior de Ingeniería y Arquitectura Unidad Tecamachalco',7),(1" +
                "35,'SEPI Escuela Superior de Ingeniería y Arquitectura Unidad Zacatenco',7),(136,'SEPI Esc" +
                "uela Superior de Medicina',7),(137,'SEPI Escuela Superior de Turismo',7),(138,'SEPI Unidad" +
                " Profesional Interdisciplinaria de Biotecnología',7),(139,'SEPI Unidad Profesional Interdi" +
                "sciplinaria de Ingeniería y Ciencias Sociales y Administrativas',7),(140,'SEPI Unidad Prof" +
                "esional Interdisciplinaria en Ingeniería y Tecnologías Avanzadas',7)";
        values = arg.split("\\),\\(");
        for(int i=0; i<values.length; i++){
            cValues = new ContentValues();
            aux = values[i];
            if(i==0)
                aux = aux.substring(1, aux.length());
            else if(i==values.length-1)
                aux = aux.substring(0, aux.length()-1);
            auxArray = aux.split(",");
            Log.d("init", "Inserting: " + aux);
            cValues.put("Nombre", auxArray[1].substring(1,auxArray[1].length()-1));
            cValues.put("idCategoria", auxArray[2]);
            dataBase.insert("Escuela", "---", cValues);
        }
    }

    public void setVotacionAsGlobal(int idVotacion, int idVotacionGlobal){
        SQLiteDatabase db;
        ContentValues values;
        db = getWritableDatabase();
        values = new ContentValues();
        values.put("idVotacionGlobal", idVotacionGlobal);
        values.put("idVotacion", idVotacion);
        values.put("Sincronizado", 1);
        db.insert("VotacionGlobal", "---", values);
        db.close();
    }

    public void sincronizaVotacion(String titulo){
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("select idVotacion from Votacion where Titulo = ?", new String[]{titulo});
        int idVotacion;
        if(c.moveToNext()){
            idVotacion = c.getInt(0);
            c.close();
            db.close();
            db = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("Sincronizado", 1);
            db.update("VotacionGlobal", values, "idVotacion = CAST(? as INTEGER)", new String[]{
                    String.valueOf(idVotacion)
            });
        }
        c.close();
        db.close();
    }

    public boolean isVotacionActualGlobal(){
        SQLiteDatabase db = getReadableDatabase();
        Cursor c;
        boolean isGlobal;
        c = db.rawQuery("select idVotacion from Votacion ORDER BY idVotacion DESC LIMIT 1", null);
        if((isGlobal = c.moveToNext())) {
            int idVotacion = c.getInt(0);
            c.close();
            c = db.rawQuery("select idVotacionGlobal from VotacionGlobal where idVotacion = CAST(? as INTEGER)",
                    new String[]{String.valueOf(idVotacion)});
            isGlobal = c.moveToFirst();
        }
        c.close();
        db.close();
        return isGlobal;
    }

    public String grabVotosForVotacion(String votacion) throws IllegalArgumentException{
        Cursor cursor = getReadableDatabase().rawQuery("select idVotacion from Votacion where Titulo = ?", new String[]{votacion});
        cursor.moveToFirst();
        int idVotacion = cursor.getInt(cursor.getColumnIndex("idVotacion"));
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query("Voto", null, "idVotacion = CAST(? as INTEGER)", new String[]{String.valueOf(idVotacion)}, null, null, null);
        JSONArray jarr = new JSONArray();
        JSONObject row;
        int counter = 0;
        while(c.moveToNext()){
            row = new JSONObject();
            try {
                row.put("idVoto", Hasher.bytesToString(c.getBlob(c.getColumnIndex("idVoto"))));
                row.put("idPerfil", c.getInt(c.getColumnIndex("idPerfil")));
                row.put("voto", c.getString(c.getColumnIndex("Voto")));
                Cursor x = getReadableDatabase().rawQuery("select Pregunta from Pregunta where idPregunta = CAST(? as INTEGER)",new String[]{String.valueOf(c.getInt(c.getColumnIndex("idPregunta")))});
                x.moveToFirst();
                row.put("pregunta",x.getString(0));
                x.close();
                jarr.put(row);
                Log.d("Mayunia", "#" + counter++ + " " + row.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        c.close();
        db.close();
        return jarr.toString();
    }

    public String grabParticipantes(){
        SQLiteDatabase db = getReadableDatabase();
        Cursor c;
        int idVotacion;
        c = db.rawQuery("select idVotacion from Votacion order by idVotacion DESC limit 1", null);
        c.moveToFirst();
        idVotacion = c.getInt(0);
        c.close();
        c = db.rawQuery(
                "select Boleta, Nombre Escuela, perfil Perfil from " +
                        "Perfil " +
                        "join " +
                        "(select Boleta, Escuela, idPrefil from" +
                        "   Escuela " +
                        "   join " +
                        "   (select * from " +
                        "       Participante p " +
                        "       join " +
                        "       (select Boleta from " +
                        "           Participante_Pregunta " +
                        "           join " +
                        "           Pregunta using(idPregunta) where idVotacion = CAST(? as INTEGER)" +
                        "       ) g using(Boleta) group by Boleta" +
                        "   ) b using(idEscuela)" +
                        ") a using(idPerfil)"
                , new String[]{String.valueOf(idVotacion)});
        JSONArray jarr = new JSONArray();
        JSONObject row;
        while(c.moveToNext()){
            try {
                row = new JSONObject();
                row.put("escuela", c.getString(c.getColumnIndex("Escuela")));
                row.put("perfil", c.getString(c.getColumnIndex("Perfil")));
                row.put("boleta", c.getString(c.getColumnIndex("Boleta")));
                jarr.put(row);
            }catch(JSONException ignore){}
        }
        c.close();
        db.close();
        return jarr.toString();
    }

    public String grabHostForUserLoginAttempt(int idLoginAttempt){ // Usado para registar una votación global.
        Cursor c = getReadableDatabase().rawQuery("select Host from LoginAttempt where idLoginAttempt = CAST(? as INTEGER)", new String[]{String.valueOf(idLoginAttempt)});
        String host = null;
        if(c.moveToFirst())
            host = c.getString(c.getColumnIndex("Host"));
        c.close();
        close();
        return host;
    }

    public int obtenerIdDeEscuela(String nombreEscuela){
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("select idEscuela from Escuela where Nombre like ?", new String[]{nombreEscuela});
        int idEscuela = -1;
        if(c.moveToFirst()){
            idEscuela = c.getInt(0);
        }
        c.close();
        db.close();
        Log.d("Get id Escuela", nombreEscuela + ": " + idEscuela);
        return idEscuela;
    }

    public String obtenerNombreDeEscuela(int idEscuela){
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("select Nombre from Escuela where idEscuela = CAST(? as INTEGER)",
                new String[]{String.valueOf(idEscuela)});
        c.moveToFirst();
        String nombre = c.getString(0);
        c.close();
        db.close();
        return nombre;
    }

    public JSONArray obtenerVotacionesConcluidas(){
        Cursor c;
        SQLiteDatabase db;
        JSONArray jelementos;
        JSONObject jelemento;
        db = getReadableDatabase();
        c = db.rawQuery("" +
                "select idVotacion, Titulo from " +
                "Votacion where concluida = 1", null);
        jelementos = new JSONArray();
        while(c.moveToNext()) {
            try {
                jelemento = new JSONObject();
                jelemento.put("idVotacion", c.getInt(c.getColumnIndex("idVotacion")));
                jelemento.put("titulo", c.getString(c.getColumnIndex("Titulo")));
                jelementos.put(jelemento);
            }catch(JSONException e){
                e.printStackTrace();
            }
        }
        c.close();
        db.close();
        return jelementos;
    }

    public Categoria[] obtenerCategorias(){
        List<Categoria> categorias = new ArrayList<>();
        Categoria categoria;
        SQLiteDatabase db;
        Cursor c;
        db = getReadableDatabase();
        c = db.rawQuery("select * from Categoria", null);
        while(c.moveToNext()) {
            categoria = new Categoria(c.getInt(c.getColumnIndex("idCategoria")));
            categoria.setNombre(c.getString(c.getColumnIndex("nombre")));
            categorias.add(categoria);
        }
        c.close();
        db.close();
        return categorias.toArray(new Categoria[]{});
    }

    public Escuela[] obtenerEscuelasPorCategoria(String categoria){
        List<Escuela> escuelas = new ArrayList<>();
        Escuela escuela;
        SQLiteDatabase db;
        Cursor c;
        db = getReadableDatabase();
        c = db.rawQuery("select * from Escuela a join Categoria b using(idCategoria) where b.nombre like ?"
                , new String[]{categoria});
        while(c.moveToNext()) {
            escuela = new Escuela(c.getInt(c.getColumnIndex("idEscuela")));
            escuela.setNombre(c.getString(c.getColumnIndex("Nombre")));
            escuela.setIdCategoria(c.getInt(c.getColumnIndex("idCategoria")));
            escuelas.add(escuela);
        }
        c.close();
        db.close();
        return escuelas.toArray(new Escuela[]{});
    }

    public Escuela[] obtenerEscuelas(){
        List<Escuela> escuelas = new ArrayList<>();
        Escuela escuela;
        SQLiteDatabase db;
        Cursor c;
        db = getReadableDatabase();
        c = db.rawQuery("select * from Escuela", null);
        while(c.moveToNext()) {
            escuela = new Escuela(c.getInt(c.getColumnIndex("idEscuela")));
            escuela.setNombre(c.getString(c.getColumnIndex("Nombre")));
            escuela.setIdCategoria(c.getInt(c.getColumnIndex("idCategoria")));
            escuelas.add(escuela);
        }
        c.close();
        db.close();
        return escuelas.toArray(new Escuela[]{});
    }

    public int grabLastUserIdAttmptSucceded(String usuario){
        int idAttempt = -1;
        Cursor c = getReadableDatabase().rawQuery("select idLoginAttempt,Attempt_Timestamp from Usuario join (select idUsuario,AttemptSucceded.idLoginAttempt,Attempt_Timestamp from LoginAttempt join AttemptSucceded using(idLoginAttempt)) r using(idUsuario) where Name like ? order by Attempt_Timestamp asc",new String[]{usuario});
        if(c.moveToLast()){
            idAttempt = c.getInt(c.getColumnIndex("idLoginAttempt"));
            c.moveToFirst();
            Log.d("Jirachi", "Consultaron: " + c.getString(c.getColumnIndex("Attempt_Timestamp")));
            while(c.moveToNext())
                Log.d("Jirachi",c.getInt(c.getColumnIndex("idLoginAttempt")) + "Consultaron: " + c.getString(c.getColumnIndex("Attempt_Timestamp")));
        }else{
            Log.d("Jirachi", "NO USERS??");
        }
        c.close();
        close();
        return idAttempt;
    }

    public int grabAdminLoginAttempt(){
        Cursor c = getReadableDatabase().rawQuery("select idLoginAttempt from LoginAttempt join Usuario on Usuario.idUsuario = LoginAttempt.idUsuario where Name = 'Administrador'",null);
        int id = -2;
        if(c.moveToNext())
            id = c.getInt(c.getColumnIndex("idLoginAttempt"));
        c.close();
        close();
        return id;
    }

    public boolean existeLoginAttemptAdmin(){
        Cursor c = getReadableDatabase().rawQuery("select * from LoginAttempt where idLoginAttempt = -1",null);
        boolean exists = (c.getCount() > 0);
        c.close();
        close();
        return exists;
    }

    public List<String> obtenerUsuarios(){
        List<String> usuarios = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery("select Name from Usuario", null);
        while(c.moveToNext()){
            usuarios.add(c.getString(0));
        }
        c.close();
        close();
        return usuarios;
    }

    public String obtenerLlaveUsuario(String usuario){
        Cursor c = getReadableDatabase().rawQuery("select Psswd from Usuario where Name like ?",
                new String[]{usuario});
        String psswd;
        if(c.moveToNext()){
            psswd = c.getString(0);
        }else psswd = null;
        c.close();
        close();
        return psswd;
    }

    public String[] consultaParticipantePreguntas(String boleta){
        SQLiteDatabase db;
        Cursor c;
        String[] preguntasFaltantes;
        db = getReadableDatabase();
        c = db.rawQuery("" +
                "select Pregunta from " +
                "Participante_Pregunta " +
                "join " +
                "(select idPregunta, Pregunta from " +
                "   Pregunta " +
                "   join " +
                "   (select idVotacion from " +
                "       Votacion order by idVotacion DESC LIMIT 1" +
                "   ) b using(idVotacion)" +
                ") a using(idPregunta) where Boleta like ? and Hora_Participacion is null"
                , new String[]{boleta});
        preguntasFaltantes = new String[c.getCount()];
        int i = 0;
        while(c.moveToNext()) {
            preguntasFaltantes[i++] = c.getString(0);
            Log.d(boleta, "Falta contestar: " + c.getString(0));
        }
        c.close();
        db.close();
        return preguntasFaltantes;
    }

    public String consultaParticipanteHoraParticipacion(String boleta){
        String[] args = {boleta};
        Cursor c = getReadableDatabase().rawQuery("select Hora_Participacion from Participante_Pregunta where Boleta = ?",args);
        String result = null;
        if(c.moveToFirst())
            result = c.getString(c.getColumnIndex("Hora_Participacion"));
        close();
        return result;
    }

    public String[] quienesHanParticipado(int idVotacion){
        SQLiteDatabase db = getReadableDatabase();
        String[] participantes;
        Cursor c = db.rawQuery("" +
                "select Boleta, Hora_Registro, Hora_Participacion from " +
                "(select * from " +
                "   Preguntas_Votacion where idVotacion = CAST(? as INTEGER)" +
                ") a " +
                "join " +
                "(select Boleta, Hora_Registro, Hora_Participacion, count(*) cuenta from " +
                "   Participante_Pregunta " +
                "   join " +
                "   (select idPregunta from " +
                "       Pregunta where idVotacion = CAST(? as INTEGER)" +
                "   ) r using(idPregunta) where Hora_Participacion is not null group by Boleta" +
                ") s on a.Preguntas = s.cuenta"
                , new String[]{String.valueOf(idVotacion), String.valueOf(idVotacion)});
        participantes = new String[c.getCount()];
        int count = 0;
        while (c.moveToNext()) {
            participantes[count++] = c.getString(c.getColumnIndex("Boleta"))+"\nRegistró: "+c.getString(c.getColumnIndex("Hora_Registro"))+"\nParticipó: "+c.getString(c.getColumnIndex("Hora_Participacion"));
        }
        c.close();
        db.close();
        return participantes;
    }

    public int insertaPerfil(String perfil, int idPerfilGlobal){
        SQLiteDatabase db;
        String[] args;
        Cursor c;
        int idPerfil;
        ContentValues values;
        db = getReadableDatabase();
        args = new String[]{perfil};
        c = db.rawQuery("select idPerfil from Perfil where perfil like ?", args);
        idPerfil = c.moveToFirst() ? c.getInt(c.getColumnIndex("idPerfil")) : -1;
        c.close();
        db.close();
        values = new ContentValues();
        values.put("idPerfilGlobal", idPerfilGlobal);
        if( idPerfil == -1 ) {
            values.put("perfil", perfil);
            db = getWritableDatabase();
            db.insert("Perfil", "---", values);
            db.close();
            db = getReadableDatabase();
            c = db.rawQuery("select last_insert_rowid()", null);
            c.moveToFirst();
            idPerfil = c.getInt(0);
            c.close();
        }else{
            db = getWritableDatabase();
            db.update("Perfil", values, "idPerfil = CAST(? as INTEGER)", new String[]{String.valueOf(idPerfil)});
            db.close();
        }
        db.close();
        return idPerfil;
    }

    public int insertaPerfil(String perfil){
        SQLiteDatabase db;
        String[] args;
        Cursor c;
        int idPerfil;
        ContentValues values;
        db = getReadableDatabase();
        args = new String[]{perfil};
        c = db.rawQuery("select idPerfil from Perfil where perfil like ?", args);
        idPerfil = c.moveToFirst() ? c.getInt(c.getColumnIndex("idPerfil")) : -1;
        c.close();
        db.close();
        values = new ContentValues();
        values.put("perfil", perfil);
        if( idPerfil == -1 ) {
            db = getWritableDatabase();
            db.insert("Perfil", "---", values);
            db.close();
            db = getReadableDatabase();
            c = db.rawQuery("select last_insert_rowid()",null);
            c.moveToFirst();
            idPerfil = c.getInt(0);
            c.close();
        }
        db.close();
        return idPerfil;
    }

    public int insertaEscuela(String nombre, Double latitud, Double longitud){
        Cursor c = getReadableDatabase().rawQuery("select * from Escuela where Nombre = ?",
                new String[]{nombre});
        int id = -1;
        if(!c.moveToFirst()) {
            ContentValues values = new ContentValues();
            values.put("Nombre", nombre);
            values.put("latitud", latitud != null ? latitud.doubleValue() : null);
            values.put("longitud", longitud != null ? longitud.doubleValue() : null);
            getWritableDatabase().insert("Escuela", "", values);
            Cursor c2 = getReadableDatabase().rawQuery("select last_insert_rowid()", null);
            if(c2.moveToFirst())
                id = c2.getInt(0);
            c2.close();
        }
        c.close();
        close();
        return id;
    }

    public boolean insertaParticipante(String boleta, String perfil, String escuela){
        Log.d("Monitor", "Insertar participante: " + boleta + ", " + perfil + ", " + escuela);
        boolean existe = false;
        String[] args = {perfil};
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("select idPerfil from Perfil where " +
                "perfil like ?", args);
        if(c.moveToFirst()){
            args[0] = escuela;
            Cursor c2 = db.rawQuery("select idEscuela from Escuela where " +
                    "Nombre like ?",args);
            if(c2.moveToFirst()){
                existe = true;
                Cursor c3 = db.rawQuery("select count(*) from Participante where Boleta like ?",
                        new String[]{boleta});
                c3.moveToFirst();
                if(c3.getInt(0) == 0) {
                    ContentValues values = new ContentValues();
                    values.put("Boleta", boleta);
                    values.put("idPerfil", c.getInt(0));
                    values.put("idEscuela", c2.getInt(0));
                    values.put("Fecha_Registro", ProveedorDeRecursos.obtenerFecha());
                    values.put("BoletaHash", new MD5Hash().makeHash(boleta));
                    try {
                        db.close();
                        db = getWritableDatabase();
                        db.insert("Participante", "---", values);
                        db.close();
                    } catch (SQLiteException e) {
                        Log.e("InsParticipante", e.getMessage());
                    }
                }else{
                    Log.d("InsParticipante", "Ya estaba el participante");
                }
                c3.close();
            }else{
                Log.d("InsParticipante", "No existe el nombre del lugar (escuela)");
            }
            c2.close();
        }else{
            Log.d("InsParticipante", "No exite el perfil");
        }
        c.close();
        db.close();
        return existe;
    }

    public int insertaNombreParticipante(String boleta, String nombre, String apPaterno, String apMaterno){
        SQLiteDatabase dbw = getWritableDatabase();
        SQLiteDatabase dbr = getReadableDatabase();
        int id;
        ContentValues values = new ContentValues();
        values.put("Boleta",boleta);
        values.put("Nombre",nombre);
        values.put("ApPaterno",apPaterno);
        values.put("ApMaterno", apMaterno);
        try {
            id = (int)dbw.insert("NombreParticipante", "---", values);
        }catch(SQLiteException e){
            e.printStackTrace();
            id = -1;
        }
        dbr.close();
        dbw.close();
        return id;
    }

    public JSONObject obtenerDatosDeVotacion(int idVotacion) throws JSONException {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("" +
                "select a.idVotacion, idEscuela, Titulo, Fecha_Inicio, Fecha_Fin, Sincronizado, concluida from " +
                "Votacion a " +
                "left join " +
                "VotacionGlobal b " +
                "on a.idVotacion = b.idVotacion where a.idVotacion = CAST(? as INTEGER)"
                , new String[]{String.valueOf(idVotacion)});
        JSONObject datosDeVotacion = new JSONObject();
        if(c.moveToNext()) {
            datosDeVotacion.put("idVotacion", c.getInt(c.getColumnIndex("idVotacion")));
            datosDeVotacion.put("idEscuela", c.getInt(c.getColumnIndex("idEscuela")));
            datosDeVotacion.put("Titulo", c.getString(c.getColumnIndex("Titulo")));
            datosDeVotacion.put("Fecha_Inicio", c.getLong(c.getColumnIndex("Fecha_Inicio")));
            datosDeVotacion.put("Fecha_Fin", c.getLong(c.getColumnIndex("Fecha_Fin")));
            datosDeVotacion.put("concluida", c.getInt(c.getColumnIndex("concluida")) != 0);
            try{
                int sincronizado = c.getInt(c.getColumnIndex("Sincronizado"));
                Log.d("ObtenerDdV", "Valor de \"Sincronizado\" obtenido: " + sincronizado);
                datosDeVotacion.put("Sincronizado", sincronizado);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        c.close();
        db.close();
        return datosDeVotacion;
    }

    public JSONObject obtenerDatosDeVotacionActual() throws JSONException {
        SQLiteDatabase db;
        Cursor c;
        boolean concluida;
        db = getReadableDatabase();
        c = db.rawQuery("select * from Votacion ORDER BY idVotacion DESC LIMIT 1", null);
        JSONObject datosDeVotacion = null;
        if(c.moveToNext()) {
            concluida = c.getInt(c.getColumnIndex("concluida")) != 0;
            if(!concluida) {
                datosDeVotacion = new JSONObject();
                datosDeVotacion.put("idVotacion", c.getInt(c.getColumnIndex("idVotacion")));
                datosDeVotacion.put("Titulo", c.getString(c.getColumnIndex("Titulo")));
                datosDeVotacion.put("Fecha_Inicio", c.getLong(c.getColumnIndex("Fecha_Inicio")));
                datosDeVotacion.put("Fecha_Fin", c.getLong(c.getColumnIndex("Fecha_Fin")));
                datosDeVotacion.put("concluida", concluida);
                datosDeVotacion.put("Soy_Propietario", c.getInt(c.getColumnIndex("Soy_Propietario")) != 0);
                Cursor x = db.rawQuery("select idVotacionGlobal, Sincronizado from VotacionGlobal where idVotacion = CAST(? as INTEGER)", new String[]{String.valueOf(c.getInt(c.getColumnIndex("idVotacion")))});
                datosDeVotacion.put("Sincronizado", x.moveToFirst() ? x.getInt(x.getColumnIndex("Sincronizado")) : -1);
                datosDeVotacion.put("idVotacionGlobal", datosDeVotacion.getInt("Sincronizado") != -1 ? x.getInt(x.getColumnIndex("idVotacionGlobal")) : -1);
                x.close();
            }
        }
        c.close();
        db.close();
        return datosDeVotacion;
    }

    public String obtenerTituloVotacionActual(){
        Cursor c = getReadableDatabase().rawQuery("" +
                "select Titulo from " +
                "   Votacion ORDER BY idVotacion DESC LIMIT 1", null);
        String titulo = null;
        if(c.moveToNext())
            titulo = c.getString(0);
        c.close();
        close();
        return titulo;
    }

    public void actualizaPregunta(String pregunta, String nuevaPregunta){
        ContentValues valores = new ContentValues();
        valores.put("Pregunta", nuevaPregunta);
        getWritableDatabase().update("Pregunta", valores, "Pregunta = ?", new String[]{pregunta});
        close();
    }

    public int insertaVotacion(String tituloVotacion, String lugar, long fechaInicio, long fechaFin, boolean soyPropietario){
        Log.d("Puritano", "We are inserting following:" + tituloVotacion + ", " + lugar + ", " + ProveedorDeRecursos.obtenerFormatoEnHoras(fechaInicio) + ", " + ProveedorDeRecursos.obtenerFormatoEnHoras(fechaFin) + ", " + soyPropietario);
        SQLiteDatabase db;
        Cursor c;
        int idVotacion = -1;
        db = getReadableDatabase();
        c = db.rawQuery("select idEscuela from Escuela where Nombre like ?", new String[]{lugar});
        if(c.moveToFirst()) {
            int idLugar = c.getInt(0);
            c.close();
            ContentValues values = new ContentValues();
            values.put("idEscuela", idLugar);
            values.put("Fecha_Inicio", fechaInicio);
            values.put("Fecha_Fin", fechaFin);
            values.put("Titulo", tituloVotacion);
            values.put("Soy_Propietario", soyPropietario ? 1 : 0);
            db = getWritableDatabase();
            Log.d("Puritano", "Row inserted number: " + db.insert("Votacion", "---", values));
            db = getReadableDatabase();
            c = db.rawQuery("select last_insert_rowid()", null);
            if(c.moveToFirst()) {
                idVotacion = c.getInt(0);
                c.close();
            }
            Log.d("Puritano", "We retrieved idVotacion: " + idVotacion);
        }
        db.close();
        c.close();
        return idVotacion;
    }

    public int insertaPregunta(String pregunta, int idVotacion){
        SQLiteDatabase dbr = getReadableDatabase();
        SQLiteDatabase dbw = getReadableDatabase();
        int id;
        Cursor c;
        ContentValues values = new ContentValues();
        values.put("Pregunta", pregunta);
        values.put("idVotacion", idVotacion);
        dbw.insert("Pregunta", "---", values);
        c = dbr.rawQuery("select last_insert_rowid()", null);
        c.moveToFirst();
        id = c.getInt(0);
        c.close();
        dbr.close();
        dbw.close();
        return id;
    }

    public int insertaOpcion(String reactivo, int idPregunta){
        SQLiteDatabase db = getWritableDatabase();
        Cursor c;
        ContentValues values = new ContentValues();
        values.put("Reactivo", reactivo);
        values.put("idPregunta", idPregunta);
        db.insert("Opcion", "---", values);
        db.close();
        db = getReadableDatabase();
        c = db.rawQuery("select last_insert_rowid()", null);
        c.moveToFirst();
        int id = c.getInt(0);
        c.close();
        db.close();
        return id;
    }

    public long insertaUsuario(String usrName, byte[] psswd){
		ContentValues values = new ContentValues();
        values.put("Name", usrName);
        values.put("Psswd", psswd);
        long result = getWritableDatabase().insert("Usuario", "---", values);
        close();
        return result;
    }

    public int insertaLoginAttempt(String usr, String host){
        String[] columns = {"idUsuario"};
        String selection = "Name = ?";
        String selArgs[] = {usr};
        Cursor c = getReadableDatabase().query("Usuario", columns, selection,selArgs,null,null,null);
        int loginAttempt = -1;
        if( c.moveToFirst() ){
            ContentValues values = new ContentValues();
            values.put("idUsuario", c.getInt(c.getColumnIndex("idUsuario")));
            values.put("Host",host);
            values.put("Attempt_Timestamp", ProveedorDeRecursos.obtenerFecha());
            getWritableDatabase().insert("LoginAttempt", "---", values);
            Cursor c2 = getReadableDatabase().rawQuery("SELECT last_insert_rowid()",null);
            c2.moveToFirst();
            loginAttempt = c2.getInt(0);
            c2.close();
        }
        c.close();
        close();
        return loginAttempt;
    }

    public long insertaAttemptSucceded(int idLoginAttempt, byte[] secretKey){
        SQLiteDatabase dbw = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("idLoginAttempt", idLoginAttempt);
        values.put("secretKey", secretKey);
        values.put("expiration_time", 864e3);
        long result = dbw.insert("AttemptSucceded", "---", values);
        dbw.close();
        return result;
    }

    public long insertaAttemptSucceded(int idLoginAttempt, byte[] secretKey, long expirationTime){
        SQLiteDatabase dbw = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("idLoginAttempt", idLoginAttempt);
        values.put("secretKey", secretKey);
        values.put("expiration_time", expirationTime);
        long result = dbw.insert("AttemptSucceded", "---", values);
        dbw.close();
        return result;
    }

    public void insertaUserAction(int idLoginAttempt, String action){
        ContentValues values = new ContentValues();
        values.put("idLoginAttempt",idLoginAttempt);
        values.put("Action", action);
        values.put("Action_Timestamp", ProveedorDeRecursos.obtenerFecha());
        getWritableDatabase().insert("UserAction", "---", values);
        close();
    }

    public long insertaParticipantePregunta(String boleta, String pregunta){
        long id = -1;
        SQLiteDatabase dbw = getWritableDatabase();
        SQLiteDatabase dbr = getReadableDatabase();
        String[] args = {pregunta};
        Cursor c = dbr.rawQuery("" +
                "select idPregunta from " +
                "Pregunta " +
                "join " +
                "(select idVotacion from " +
                "   Votacion order by idVotacion DESC LIMIT 1" +
                ")a using(idVotacion) " +
                "where Pregunta like ?", args);
        Log.d("Marakeru", "We've been asked to insert: " + pregunta + " for " + boleta);
        if(c.moveToFirst()){
            ContentValues values = new ContentValues();
            values.put("Boleta", boleta);
            values.put("idPregunta", c.getInt(c.getColumnIndex("idPregunta")));
            values.put("Hora_Registro", ProveedorDeRecursos.obtenerFecha());
            id = dbw.insert("Participante_Pregunta","---",values);
            if(id != -1)
                Log.d("Inserta Participante","Participante insertado exitosamente.");
            else
                Log.d("Inserta Participante", "El participante ya había sido agregado -.-");
        }else
            Log.d("Marakeru","We were not able to register " + pregunta + " for " + boleta);
        c.close();
        dbr.close();
        dbw.close();
        return id;
    }

    public boolean actualizaParticipantePregunta(String boleta, String pregunta){
        boolean success = false;
        SQLiteDatabase dbr = getReadableDatabase();
        SQLiteDatabase dbw = getWritableDatabase();
        Cursor c;
        c = dbr.rawQuery("select idPregunta from " +
                "Pregunta " +
                "join " +
                "(select idVotacion from " +
                "   Votacion order by idVotacion DESC LIMIT 1" +
                ") a using(idVotacion) " +
                "where Pregunta like ?", new String[]{pregunta});
        if(c.moveToFirst()) {
            int idPregunta = c.getInt(0);
            ContentValues values = new ContentValues();
            values.put("Hora_Participacion", ProveedorDeRecursos.obtenerFecha());
            success = dbw.update("Participante_Pregunta", values
                    , "Boleta like ? and idPregunta = CAST(? as INTEGER)"
                    , new String[]{boleta,String.valueOf(idPregunta)}) > 0;
        }
        dbr.close();
        dbw.close();
        c.close();
        return success;
    }

    public String obtenerTituloVotacionFromId(int idVotacion){
        Cursor c = getReadableDatabase().rawQuery("select Titulo from Votacion where idVotacion = CAST(? as INTEGER)", new String[]{String.valueOf(idVotacion)});
        String titulo = null;
        if(c.moveToFirst())
            titulo = c.getString(c.getColumnIndex("Titulo"));
        c.close();
        close();
        return titulo;
    }

    public int obtenerIdVotacionActual(){
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("select idVotacion from Votacion order by idVotacion DESC LIMIT 1", null);
        int idVotacion = c.moveToFirst() ? c.getInt(0) : -1;
        c.close();
        db.close();
        return idVotacion;
    }

    public byte[] obtenerSKeyEncoded(int idAttempt){
        byte[] encodedSKey = null;
        Cursor c = getReadableDatabase().rawQuery("SELECT secretKey from AttemptSucceded where idLoginAttempt = CAST(? as INTEGER)", new String[]{String.valueOf(idAttempt)});
        if(c.moveToFirst()){
            encodedSKey = c.getBlob(c.getColumnIndex("secretKey"));
            c.close();
        }
        close();
        return encodedSKey;
    }

    public void dummySelect(){
        Cursor c = getReadableDatabase().rawQuery("SELECT * FROM AttemptSucceded", null);
        Cursor c2 = getReadableDatabase().rawQuery("SELECT secretKey FROM AttemptSucceded WHERE idLoginAttempt = CAST(? as INTEGER)", new String[]{String.valueOf(9)});
        if(c2.moveToNext())
            Log.e("Riot Mode",Arrays.toString(c2.getBlob(c2.getColumnIndex("secretKey"))));
        else {
            Log.e("Termination", "Sucks");
            c2 = getReadableDatabase().query("AttemptSucceded", null, "idLoginAttempt=?",
                    new String[]{"13"}, null, null, null);
            if(c2.moveToNext())
                Log.i("Intrinsic",Arrays.toString(c2.getBlob(c2.getColumnIndex("secretKey"))));
            else
                Log.i("Intrinsic","It keeps sucking");
        }
        c2.close();
        while(c.moveToNext())
            Log.d("STRIKER",c.getInt(c.getColumnIndex("idLoginAttempt"))+Arrays.toString(c.getBlob(c.getColumnIndex("secretKey"))));
        close();
    }

    public long insertaVoto(byte[] idVoto, int idVotacion, String perfil, String voto, int idLoginAttempt, int idPregunta){
        SQLiteDatabase db = getWritableDatabase();
        long id = -1;
        String args[] = {perfil};
        Cursor c = getReadableDatabase().rawQuery("select idPerfil from Perfil where perfil = ?",args);
        if(c.moveToFirst()){
            ContentValues values = new ContentValues();
            values.put("idVoto", idVoto);
            values.put("idVotacion", idVotacion);
            values.put("idPerfil", c.getInt(c.getColumnIndex("idPerfil")));
            values.put("Voto", voto);
            values.put("idLoginAttempt", idLoginAttempt);
            values.put("idPregunta", idPregunta);
            id = db.insert("Voto", "---", values);
        }
        c.close();
        db.close();
        return id;
    }

    public long insertaVoto2(byte[] idVoto, int idPerfil, String voto, String pregunta){
        long id = -1;
        ContentValues va = new ContentValues();
        va.put("Pregunta","Con relación al servicio de la cafetería de la UPIITA:");
        getWritableDatabase().update("Pregunta",va,"Pregunta = ?",new String[]{"Con relación al servicio de la cafetería de la upiita:"});
        Cursor c = getReadableDatabase().rawQuery("select idVotacion,idPregunta from Pregunta " +
                "where Pregunta = ?", new String[]{pregunta.trim()});
        Cursor c2 = getReadableDatabase().rawQuery("select idLoginAttempt from " +
                "AttemptSucceded",null);
        c2.moveToLast();
        c.moveToFirst();
        ContentValues values = new ContentValues();
        values.put("idVoto",idVoto);
        values.put("idVotacion",c.getInt(c.getColumnIndex("idVotacion")));
        values.put("idPerfil",idPerfil);
        values.put("Voto",voto);
        values.put("idLoginAttempt",c2.getInt(0));
        values.put("idPregunta", c.getInt(c.getColumnIndex("idPregunta")));
        id = getWritableDatabase().insert("Voto", "---", values);
        c.close();
        c2.close();
        close();
        return id;
    }

	public boolean consultaUsuario(String usrName, byte[] psswd){
		boolean result = false;
		String[] args = {usrName};
        SQLiteDatabase rdb = getReadableDatabase();
        if( rdb == null){
            Log.d("Fury", "Why!?");
            return false;
        }
        Cursor c = rdb.rawQuery("select Psswd from Usuario where Name = ?", args);
        if (c.moveToFirst()) {
            Log.d("From consulta usuario", "Comparing the arrays... " + Arrays.toString(psswd) + " **vs** " + Arrays.toString(c.getBlob(0)));
            result = Arrays.equals(c.getBlob(0), psswd);
        }
        c.close();
		close();
		return result;
	}

    public boolean consultaEscuela(){
        boolean result = getReadableDatabase().rawQuery("select * from Escuela", null).getCount()
                > 0;
        close();
        return result;
    }

    public boolean consultaPerfiles(){
        boolean result = getReadableDatabase().rawQuery("select * from Perfil", null).getCount()
                > 0;
        close();
        return result;
    }

    public boolean actualizaUsuarioPsswd(String usrName, byte[] psswd){
        ContentValues values = new ContentValues();
        values.put("Psswd", psswd);
        String[] selArgs = {usrName};
        boolean result = getWritableDatabase().update("Usuario", values, "Name=?", selArgs) > 0;
        close();
        return result;
    }

    public int obtenerCantidadParticipantes(int idVotacion){
        SQLiteDatabase db = getReadableDatabase();
        int cant = 0;
        Cursor c;
        c = db.rawQuery("" +
                "select count(*) from " +
                "(select count(*) total from " +
                "   Pregunta where idVotacion = CAST(? as INTEGER)" +
                ") a " +
                "join " +
                "(select Boleta, Hora_Registro, Hora_Participacion, count(*) total from " +
                "   Participante_Pregunta " +
                "   join " +
                "   (select idPregunta from " +
                "       Pregunta where idVotacion = CAST(? as INTEGER)" +
                "   ) r using(idPregunta) where Hora_Participacion is not null " +
                "       group by Boleta" +
                ") s using(total)"
                , new String[]{String.valueOf(idVotacion),String.valueOf(idVotacion)});
        if (c.moveToNext()) {
            cant = c.getInt(0);
        }
        c.close();
        db.close();
        return cant;
    }

    public void borraPerfil(String perfil){
        String[] args = {perfil};
        getWritableDatabase().delete("Perfil", "perfil = ?", args);
        close();
    }

    public String obtenerPerfilDeUsuario(String boleta){
        String perfil = null;
        Cursor c = getReadableDatabase().rawQuery("" +
                "select perfil from " +
                "Perfil " +
                "join " +
                "Participante using(idPerfil) where Boleta like ? or BoletaHash like ?", new String[]{boleta, boleta});
        if(c.moveToFirst())
            perfil = c.getString(c.getColumnIndex("perfil"));
        c.close();
        close();
        return perfil;
    }

    public String[] obtenerPerfiles(){
        SQLiteDatabase db;
        Cursor c;
        db = getReadableDatabase();
        c = db.rawQuery("select perfil from Perfil where perfil not like 'NaN'",null);
        String[] perfiles = new String[c.getCount()];
        int counter = 0;
        while(c.moveToNext())
            perfiles[counter++] = c.getString(c.getColumnIndex("perfil"));
        c.close();
        db.close();
        return perfiles;
    }

    public String[] obtenerPerfilesDeVotacion(int idVotacion){
        SQLiteDatabase db;
        Cursor c;
        db = getReadableDatabase();
        c = db.rawQuery("select perfil from " +
                "(select idPerfil from " +
                "   Voto where idVotacion = CAST(? as INTEGER) group by idPerfil" +
                ")b " +
                "join " +
                "Perfil using(idPerfil)", new String[]{String.valueOf(idVotacion)});
        String[] perfiles = new String[c.getCount()];
        int counter = 0;
        while(c.moveToNext())
            perfiles[counter++] = c.getString(c.getColumnIndex("perfil"));
        c.close();
        db.close();
        return perfiles;
    }

    public String obtenerUsuarioPorIdAttempt(int idAttempt){
        String uName = null;
        Cursor c = getReadableDatabase().rawQuery("Select Name from (Usuario join LoginAttempt on " +
                "Usuario.idUsuario = LoginAttempt.idUsuario) t where  t.idLoginAttempt = ?",new String[]{String.valueOf(idAttempt)});
        if(c.moveToFirst()){
            uName = c.getString(c.getColumnIndex("Name"));
        }
        c.close();
        close();
        return uName;
    }

    public String[] obtenerLog(){
        String[] logRows;
        Cursor c = getReadableDatabase().rawQuery("select * from UserAction", null);
        logRows = new String[c.getCount()];
        int i = 0;
        while(c.moveToNext()){
            logRows[i++] = c.getInt(c.getColumnIndex("idUserAction")) + "@" + c.getInt(c.getColumnIndex("idLoginAttempt")) + "@" + c.getString(c.getColumnIndex("Action")) + "@" + c.getString(c.getColumnIndex("Action_Timestamp"));
        }
        c.close();
        close();
        return logRows;
    }

    public boolean consultaExistenciaBoleta(String boleta){
        boolean existe;
        SQLiteDatabase db = getReadableDatabase();
        Cursor c;
        c = db.rawQuery("" +
                "select count(*) from " +
                "Participante_Pregunta " +
                "join " +
                "(select * from " +
                "   Pregunta " +
                "   join " +
                "   (select idVotacion from " +
                "      votacion order by idVotacion desc limit 1" +
                "   ) a using(idVotacion)" +
                ") r using(idPregunta) where Boleta like ?"
                , new String[]{boleta});
        c.moveToFirst();
        existe = c.getInt(0) > 0;
        c.close();
        db.close();
        Log.d("ComprobandoBol", "Existe? " + existe);
        return existe;
    }

    public boolean consultaRemotaExistenciaBoleta(String boleta){
        SQLiteDatabase db = getReadableDatabase();
        boolean existe = false;
        Cursor c = db.rawQuery("select * from Participante where BoletaHash like ?", new String[]{boleta});
        if(c.getCount() > 0 && c.moveToFirst()){
            Cursor c2 = db.rawQuery("select idVotacion from Votacion order by idVotacion DESC limit 1", null);
            if(c2.moveToFirst()){
                Cursor c3 = db.rawQuery("select count(*) from " +
                        "Participante_Pregunta " +
                        "join " +
                        "(select * from " +
                        "   Pregunta where Pregunta.idVotacion = CAST(? as INTEGER)" +
                        ") r on r.idPregunta = Participante_Pregunta.idPregunta where Boleta like ?"
                        , new String[]{String.valueOf(c2.getInt(c2.getColumnIndex("idVotacion"))), c.getString(c.getColumnIndex("Boleta"))});
                c3.moveToFirst();
                existe = c3.getInt(0) > 0;
                c3.close();
            }
            c2.close();
        }
        c.close();
        db.close();
        return existe;
    }

    public int obtenerTotalNumParticipantes(int idVotacion){
        Cursor c = getReadableDatabase().rawQuery("" +
                "select count(*) from " +
                "(select idPregunta from " +
                "   Participante_Pregunta " +
                "   join " +
                "   (select idPregunta from " +
                "       Pregunta where idVotacion = CAST(? as INTEGER)" +
                "   )b using(idPregunta)" +
                ")c group by idPregunta"
                ,new String[]{String.valueOf(idVotacion)});
        int cantidad = -1;
        if(c.moveToFirst())
            cantidad = c.getInt(0);
        c.close();
        close();
        Log.d("LeyendaDelDragon", "La leyenda del dragón blanco --> " + cantidad);
        return cantidad;
    }

    public int obtenerIdPregunta(String pregunta, int idVotacion){
        int idPregunta = -1;
        Cursor c = getReadableDatabase().rawQuery("" +
                "select idPregunta from Pregunta where Pregunta like ? and idVotacion = CAST(? as INTEGER)"
                , new String[]{pregunta, String.valueOf(idVotacion)});
        if(c.moveToFirst()){
            idPregunta = c.getInt(c.getColumnIndex("idPregunta"));
        }
        c.close();
        close();
        return idPregunta;
    }

    public Map<String,Integer> obtenerResultadosPorPregunta(int idPregunta, int idVotacion){
        SQLiteDatabase db = getReadableDatabase();
        Cursor c;
        Map<String, Integer> resultadosPorOpcion = new TreeMap<>();
        c = db.rawQuery("select Voto, count(*) c from Voto where idPregunta = CAST(? as INTEGER) and idVotacion = CAST(? as INTEGER) group by Voto"
                        ,new String[]{String.valueOf(idPregunta), String.valueOf(idVotacion)});
        while(c.moveToNext()){
            resultadosPorOpcion.put(c.getString(c.getColumnIndex("Voto")),c.getInt(c.getColumnIndex("c")));
        }
        c.close();
        db.close();
        return resultadosPorOpcion;
    }

    public Map<String,Integer> obtenerResultadosPorPreguntaPorPerfil(int idPregunta, String perfil, int idVotacion){
        SQLiteDatabase db = getReadableDatabase();
        Cursor c;
        c = db.rawQuery("select idPerfil from Perfil where perfil like ?", new String[]{perfil});
        c.moveToFirst();
        int idPerfil = c.getInt(0);
        c.close();
        Map<String, Integer> resultadosPorOpcion = new TreeMap<>();
        c = db.rawQuery("select Voto, count(*) c from " +
                        "Voto where idVotacion = CAST(? as INTEGER) " +
                        "   and idPregunta = CAST(? AS INTEGER) " +
                        "   and idPerfil = CAST(? as INTEGER) group by Voto",
                new String[]{String.valueOf(idVotacion), String.valueOf(idPregunta), String.valueOf(idPerfil)});
        while(c.moveToNext()){
            resultadosPorOpcion.put(c.getString(c.getColumnIndex("Voto")), c.getInt(c.getColumnIndex("c")));
        }
        c.close();
        db.close();
        return resultadosPorOpcion;
    }

    public Pregunta[] obtenerPreguntasVotacionActual(){
        Votacion votacion;
        Pregunta pregunta;
        Opcion opcion;
        Cursor c;
        c = getReadableDatabase().rawQuery("select idVotacion, idPregunta, Pregunta, idOpcion, Reactivo from " +
                "(select idPregunta, Pregunta, idVotacion from " +
                "   Pregunta " +
                "   join " +
                "   (select idVotacion from " +
                "       Votacion order by idVotacion DESC limit 1" +
                "   ) r using(idVotacion)" +
                ")a " +
                "join " +
                "Opcion using(idPregunta) order by idPregunta ASC"
                , null);
        votacion = new Votacion();
        while(c.moveToNext()){
            pregunta = new Pregunta(c.getInt(c.getColumnIndex("idPregunta")));
            pregunta.setEnunciado(c.getString(c.getColumnIndex("Pregunta")));
            pregunta.setIdVotacion(c.getInt(c.getColumnIndex("idVotacion")));
            votacion.agregarPregunta(pregunta);
            opcion = new Opcion();
            opcion.setId(c.getInt(c.getColumnIndex("idOpcion")));
            opcion.setReactivo(c.getString(c.getColumnIndex("Reactivo")));
            opcion.setIdPregunta(pregunta.getId());
            votacion.agregarOpcion(votacion.buscarPregunta(pregunta.getEnunciado()), opcion.getReactivo());
        }
        c.close();
        close();
        return votacion.getPreguntas().toArray(new Pregunta[]{});
    }

    public Pregunta[] obtenerPreguntasDeVotacion(int idVotacion){
        Votacion votacion;
        Pregunta pregunta;
        Opcion opcion;
        Cursor c;
        c = getReadableDatabase().rawQuery("select idVotacion, idPregunta, Pregunta, idOpcion, Reactivo from " +
                "(select idPregunta, Pregunta, idVotacion from " +
                "   Pregunta where idVotacion = CAST(? as INTEGER)" +
                ")a " +
                "join " +
                "Opcion using(idPregunta) order by idPregunta ASC"
                , new String[]{String.valueOf(idVotacion)});
        votacion = new Votacion();
        while(c.moveToNext()){
            pregunta = new Pregunta(c.getInt(c.getColumnIndex("idPregunta")));
            pregunta.setEnunciado(c.getString(c.getColumnIndex("Pregunta")));
            pregunta.setIdVotacion(c.getInt(c.getColumnIndex("idVotacion")));
            votacion.agregarPregunta(pregunta);
            opcion = new Opcion();
            opcion.setId(c.getInt(c.getColumnIndex("idOpcion")));
            opcion.setReactivo(c.getString(c.getColumnIndex("Reactivo")));
            opcion.setIdPregunta(pregunta.getId());
            votacion.agregarOpcion(votacion.buscarPregunta(pregunta.getEnunciado()), opcion.getReactivo());
        }
        c.close();
        close();
        return votacion.getPreguntas().toArray(new Pregunta[]{});
    }

    public JSONArray obtenerOpcionesPregunta(String pregunta){
        SQLiteDatabase db = getReadableDatabase();
        Cursor c;
        int idVotacion;
        c = db.rawQuery("select idVotacion from Votacion order by idVotacion DESC LIMIT 1", null);
        c.moveToFirst();
        idVotacion = c.getInt(0);
        c.close();
        c = db.rawQuery("" +
                "SELECT Reactivo from " +
                "Pregunta " +
                "join " +
                "Opcion " +
                "using(idPregunta) where idVotacion = CAST(? as INTEGER) and Pregunta like ?"
                , new String[]{String.valueOf(idVotacion), pregunta});
        JSONArray opciones = new JSONArray();
        while(c.moveToNext()){
            opciones.put(c.getString(0));
        }
        c.close();
        db.close();
        return opciones;
    }

    public byte[] obtenerKeyByHost(String host, String usrName){
        Cursor c = getReadableDatabase().rawQuery("" +
                "select idLoginAttempt, secretKey, Attempt_Timestamp from (select LoginAttempt.idLoginAttempt, Attempt_Timestamp,secretKey,idUsuario from LoginAttempt join AttemptSucceded using(idLoginAttempt) where Host like ?)a join Usuario using(idUsuario) where Name like ? order by idLoginAttempt DESC limit 1", new String[]{"%" + host + "%", "%" + usrName + "%"});
        byte[] key = null;
        if(c.moveToNext()){
            key = c.getBlob(c.getColumnIndex("secretKey"));
            Log.d("ObtenerKeyByHost","Obteniendo key para " + usrName + " desde " + host + ": " + c.getInt(c.getColumnIndex("idLoginAttempt")) + " - " + c.getString(c.getColumnIndex("Attempt_Timestamp")) + " --> " + Arrays.toString(key));
        }
        Log.d("ObtenerKeyByHost", "Hecho.");
        c.close();
        close();
        return key;
    }
	
	/*********
	 * 
	 * 
	 * KEY HANDLER
	 * 
	 * 
	 * *****************/

	public boolean revisaExistenciaDeCredencial(String nombre){
		String[] args = {nombre};
        Cursor c = getReadableDatabase().rawQuery("select idUsuario from Usuario where Name = ?", args);
		boolean success = c.getCount() > 0 ? true : false;
        c.close();
		close();
		return success;
	}

	public long insertaRegistro(String escuela, String[] preguntas){
		SQLiteDatabase dbw = getWritableDatabase();
        SQLiteDatabase dbr = getReadableDatabase();
        BufferedReader bf;
        String row;
        String elements[];
        String formatedTime;
        ContentValues values;
        ContentValues vl;
        Calendar calendar;
        long id = -1;
        int mes;
        int diaDelMes;
        int hora;
        int minuto;
        int segundo;
        int miliSegundos;
        int count = 1;
        Cursor c = dbr.rawQuery("select idEscuela from Escuela where Nombre = ?",
                new String[]{escuela});
        if(c.moveToFirst())
		try{
            bf = new BufferedReader(new InputStreamReader(new FileInputStream(new File(FILE_NAME))));
            while((row = bf.readLine()) != null){
				elements = row.split(",");
				try{
                    values = new ContentValues();
                    values.put("Boleta", elements[0]);
                    values.put("BoletaHash",new MD5Hash().makeHash(elements[0]));
                    values.put("idEscuela",c.getInt(0));
                    Cursor x = dbr.rawQuery("select idPerfil from Perfil where perfil = ?",
                            new String[]{elements[4]});
                    if(!x.moveToFirst()) {
                        vl = new ContentValues();
                        vl.put("perfil",elements[4]);
                        dbw.insert("Perfil", "---", vl);
                    }
                    x.close();
                    x = dbr.rawQuery("select count(*) from Participante where Boleta like ?",
                            new String[]{elements[0]});
                    if(x.moveToFirst() && x.getInt(0) == 0) {
                        x.close();
                        x = dbr.rawQuery("select idPerfil from Perfil where perfil = ?",
                                new String[]{elements[4]});
                        x.moveToNext();
                        values.put("idPerfil", x.getInt(x.getColumnIndex("idPerfil")));
                        calendar = Calendar.getInstance();
                        hora = calendar.get(Calendar.HOUR_OF_DAY);
                        minuto = calendar.get(Calendar.MINUTE);
                        segundo = calendar.get(Calendar.SECOND);
                        miliSegundos = calendar.get(Calendar.MILLISECOND);
                        formatedTime = (hora < 10 ? "0" + hora : hora)
                                + ":" + (minuto < 10 ? "0" + minuto : minuto)
                                + ":" + (segundo < 10 ? "0" + segundo :
                                        segundo)
                                + "." + (miliSegundos < 10 ? "00" + miliSegundos
                                            : miliSegundos < 100 ? "0" + miliSegundos
                                                : miliSegundos);
                        values.put("Fecha_Registro", formatedTime);
                        dbw.insert("Participante", "---", values);
                    }
                    x.close();
                    x = dbr.rawQuery("select count(*) from NombreParticipante where Boleta like ?",
                            new String[]{elements[0]});
                    x.moveToNext();
                    if(x.getInt(0) == 0) {
                        values = new ContentValues();
                        values.put("Boleta", elements[0]);
                        values.put("Nombre", elements[3]);
                        values.put("ApPaterno", elements[1]);
                        values.put("ApMaterno", elements[2]);
                        id = dbw.insert("NombreParticipante", "---", values);
                    }
                    for(String pregunta : preguntas){
                        x.close();
                        x = dbr.rawQuery("select idPregunta from Pregunta where Pregunta = ?",
                                new String[]{pregunta});
						x.moveToFirst();
                        values = new ContentValues();
						values.put("Boleta", elements[0]);
						values.put("idPregunta", x.getInt(x.getColumnIndex("idPregunta")));
                        calendar = Calendar.getInstance();
                        mes = calendar.get(Calendar.MONTH);
                        diaDelMes = calendar.get(Calendar.DAY_OF_MONTH);
                        hora = calendar.get(Calendar.HOUR_OF_DAY);
                        minuto = calendar.get(Calendar.MINUTE);
                        segundo = calendar.get(Calendar.SECOND);
                        miliSegundos = calendar.get(Calendar.MILLISECOND);
                        formatedTime = (diaDelMes < 10 ? "0" + diaDelMes : diaDelMes)
                                + "/" + (mes < 10 ? "0" + mes : mes)
                                + "/" + calendar.get(Calendar.YEAR)
                                + " " + (hora < 10 ? "0" + hora : hora)
                                + ":" + (minuto < 10 ? "0" + minuto : minuto)
                                + ":" + (segundo < 10 ? "0" + segundo : segundo)
                                + "." + (miliSegundos < 10 ? "00" + miliSegundos
                                            : miliSegundos < 100 ? "0" + miliSegundos
                                                : miliSegundos);
                        values.put("Hora_Registro", formatedTime);
                        dbw.insert("Participante_Pregunta", "---", values);
					}
                    x.close();
                    Log.d("EL MAN","" + count++);
				}catch(SQLException e){
					if(e.toString().contains("primary")){
						Log.d("CAPIZ:","There was a bad input field: " + elements[0]);
					}
					e.printStackTrace();
				}
			}
			bf.close();
		}catch(IOException e){
			e.printStackTrace();
		}
        c.close();
		dbw.close();
        dbr.close();
		return id;
	}
	
	public boolean consultaParticipanteHaVotado(String boleta){
        SQLiteDatabase db = getReadableDatabase();
		Cursor c = getReadableDatabase().rawQuery("" +
                "select count(*) preguntas_contestadas from " +
                "(select idPregunta from" +
                "   (select idVotacion from " +
                "       Votacion order by idVotacion DESC limit 1" +
                "   )r" +
                "   join " +
                "   Pregunta using(idVotacion)" +
                ")t " +
                "join " +
                "Participante_Pregunta using(idPregunta) where Hora_Participacion is not null " +
                "   and Boleta LIKE ? " +
                "   group by Boleta", new String[]{boleta});
        c.moveToFirst();
        int preguntasContestadas = c.getInt(0);
        c.close();
        c = db.rawQuery("select Preguntas from Preguntas_Votacion join" +
                " (select idVotacion from Votacion order by idVotacion DESC limit 1)r using(idVotacion)", null);
        c.moveToFirst();
        int cantidadPreguntasVotacion = c.getInt(0);
        c.close();
        db.close();
        Log.d("DragonForce", "" + preguntasContestadas + " vs " + cantidadPreguntasVotacion);
        boolean haConcluido = cantidadPreguntasVotacion == preguntasContestadas;
        if(haConcluido){
            db = getWritableDatabase();
            ContentValues values = new ContentValues();

        }
		return haConcluido;
	}
	
	public String[] consultaVotando(int idVotacion) {
        SQLiteDatabase db = getReadableDatabase();
        String[] participantes;
        Cursor c = db.rawQuery("" +
                "select Boleta, idPregunta, Pregunta, Hora_Registro, Hora_Participacion from " +
                "(select * from " +
                "   Preguntas_Votacion where idVotacion = CAST(? as INTEGER)" +
                ") a " +
                "join " +
                "(select Boleta, Pregunta, idPregunta, Hora_Registro, Hora_Participacion, count(*) cuenta from " +
                "   Participante_Pregunta " +
                "   join " +
                "   (select idPregunta, Pregunta from " +
                "       Pregunta where idVotacion = CAST(? as INTEGER)" +
                "   ) r using(idPregunta) where " +
                "       Hora_Participacion is not null group by Boleta" +
                ") s on (Preguntas > cuenta)"
                , new String[]{String.valueOf(idVotacion), String.valueOf(idVotacion)});
        participantes = new String[c.getCount()];
        int count = 0;
        while (c.moveToNext()) {
            //participantes[count++] = c.getString(c.getColumnIndex("Boleta"))+", Pregunta: "+ c.getString(c.getColumnIndex("Pregunta")) +", HR: "+c.getString(c.getColumnIndex("Hora_Registro"))+", HP: "+c.getString(c.getColumnIndex("Hora_Participacion"));
            participantes[count++] = c.getString(c.getColumnIndex("Boleta"))+","+ c.getInt(c.getColumnIndex("idPregunta"))+","+ c.getString(c.getColumnIndex("Pregunta"));
        }
        c.close();
        db.close();
        return participantes;
	}
	
	public String[] consultaVoto(int idVotacion) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c;
		c = db.query("Voto", null, "idVotacion = CAST(? as INTEGER)", new String[]{String.valueOf(idVotacion)}, null, null, null);
		String rows[] = new String[c.getCount()];
		int index = 0;
		while(c.moveToNext())
			rows[index++] = Hasher.bytesToString(c.getBlob(c.getColumnIndex("idVoto")))+"@Votacion: "+c.getInt(c.getColumnIndex("idVotacion"))
                    +"@idPerfil: "+c.getInt(c.getColumnIndex("idPerfil"))+"@Voto: "+c.getString(c.getColumnIndex("Voto"))
                    +"@idLoginAttempt: "+c.getInt(c.getColumnIndex("idLoginAttempt"))+"@idPregunta: "+c.getInt(c.getColumnIndex("idPregunta"));
		db.close();
        c.close();
		return rows;
	}


    /*
    *
    * Elimina todos los datos de votos de la base de datos
    *
    */
	public void terminarProceso(){
		SQLiteDatabase db = getWritableDatabase();
		db.delete("Voto", "1", null);
        db.delete("LoginAttempt", "1", null);
		db.close();
	}

    public JSONArray mergeResults(String titulo){
        Cursor c = getReadableDatabase().rawQuery("select idVotacion from Votacion where Titulo = ?", new String[]{titulo});
        c.moveToNext();
        int idVotacion = c.getInt(c.getColumnIndex("idVotacion"));
        c.close();
        c = getReadableDatabase().rawQuery("select * from Voto where idVotacion = CAST(? as INTEGER)",new String[]{String.valueOf(idVotacion)});
        JSONArray jarr = new JSONArray();
        JSONObject json;
        while(c.moveToNext()){
            json = new JSONObject();
            for(int i=0; i < c.getColumnCount(); i++)
                switch(c.getType(i)) {
                    case Cursor.FIELD_TYPE_BLOB:
                        try {
                            json.put(c.getColumnName(i), Hasher.bytesToString(c.getBlob(i)));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    case Cursor.FIELD_TYPE_INTEGER:
                        try{
                            json.put(c.getColumnName(i),c.getInt(i));
                        }catch(JSONException e){
                            e.printStackTrace();
                        }
                        break;
                    case Cursor.FIELD_TYPE_STRING:
                        try{
                            json.put(c.getColumnName(i),c.getString(i));
                        }catch(JSONException e){
                            e.printStackTrace();
                        }
                        break;
                }
            jarr.put(json);
        }
        c.close();
        close();
        return jarr;
    }
	
	/*********
	 * 
	 * 
	 * PARTICIPANTE
	 * 
	 * 
	 * *****************/

	public long altaParticipante(String boleta, String nombre, String perfil){
		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("Boleta", boleta);
		values.put("Nombre", nombre);
		values.put("Perfil", perfil);
		values.put("Fecha_Registro", new SimpleDateFormat("dd/mm/yyyy hh:MM:ss").format(new Date()));
		values.put("Fecha_Ultima_Modificacion", new SimpleDateFormat("dd/mm/yyyy hh:MM:ss").format(new Date()));
		long result = -1;
		try{
			result = db.insertOrThrow("Participante","---",values);
		}catch(Exception e){
			Log.d("Cuchele","Se ha repetido la boleta: " + boleta + ". "+e.toString());
		}
		db.close();
		return result;
	}

	public boolean quitarParticipante(String boleta){
		SQLiteDatabase db = getWritableDatabase();
		String whereClause = "Boleta=?";
		String[] whereArgs = {boleta};
		int count = db.delete("Participante",whereClause,whereArgs);
		db.close();
        return count > 0;
	}

	public boolean cambiaParticipante(String boleta, String nuevaBoleta, String nuevoNombre, String nuevoPerfil){
		SQLiteDatabase db = getWritableDatabase();
		String whereClause = "Boleta=?";
		String whereArgs[] = {boleta};
		ContentValues values = new ContentValues();
		values.put("Boleta", nuevaBoleta);
		values.put("Nombre", nuevoNombre);
		values.put("Perfil",nuevoPerfil);
		values.put("Fecha_Ultima_Modificacion", new SimpleDateFormat("dd/mm/yyyy hh:MM:ss").format(new Date()));
        return db.update("Participante", values, whereClause, whereArgs) > 0;
	}

	public String[] obtenerParticipantes(){
        Cursor c = getReadableDatabase().rawQuery("SELECT * from Usuario", null);
        String[] participantes = new String[c.getCount()];
        int count = 0;
        while(c.moveToNext())
            participantes[count++] = c.getString(c.getColumnIndex("Name"));
		return participantes;
	}
	
	/*********
	 * 
	 * 
	 * PARTICIPANTE_PERFIL
	 * 
	 * 
	 * *****************/

	public boolean agregarParticipantePerfil(String idParticipante, String idPerfil){
		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("Participante", idParticipante);
		values.put("Perfil", idPerfil);
		long result = db.insert("Participante_Perfil", "---", values);
		db.close();
        return result != -1;
	}

	public boolean quitarParticipantePerfil(String idParticipante, String idPerfil){
		SQLiteDatabase db = getWritableDatabase();
		String whereClause = "Participante=? and Perfil=?";
		String[] whereArgs = {idParticipante, idPerfil};
		int count = db.delete("Participante_Perfil", whereClause, whereArgs);
		db.close();
        return count > 0;
	}
	
	/*********
	 * 
	 * 
	 * PARTICIPANTE_PREGUNTA
	 * 
	 * 
	 * *****************/

	public boolean agregarParticipantePregunta(String idParticipante, String idPregunta){
		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("Participante", idParticipante);
		values.put("Pregunta", idPregunta);
		long result = db.insert("Participante_Pregunta", "---", values);
		db.close();
        return result != -1;
	}

	public boolean quitarParticipantePregunta(String idParticipante, String idPregunta){
		SQLiteDatabase db = getWritableDatabase();
		String whereClause = "Participante=? and Pregunta=?";
		String[] whereArgs = {idParticipante, idPregunta};
		int count = db.delete("Participante_Pregunta", whereClause, whereArgs);
		db.close();
        return count > 0;
	}
	
	/*********
	 * 
	 * 
	 * PERFIL
	 * 
	 * 
	 * *****************/

	public boolean altaPerfil(String nombre){
		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("Nombre", nombre);
		long result = db.insert("Perfil", "---", values);
		db.close();
        return result != -1;
	}

	public boolean quitarPerfil(String nombre){
		SQLiteDatabase db = getWritableDatabase();
		String whereClause = "perfil=?";
		String[] whereArgs = {nombre};
		int count = db.delete("Perfil", whereClause, whereArgs);
		db.close();
        return count > 0;
	}

	public boolean cambiaPerfil(String nombre){
		SQLiteDatabase db = getWritableDatabase();
		String whereClause = "Nombre=?";
		String whereArgs[] = {nombre};
		ContentValues values = new ContentValues();
		values.put("Nombre", nombre);
        return db.update("Perfil", values, whereClause, whereArgs) > 0;
	}

    public boolean revisaExistenciaDePerfiles(){
        boolean hayAlgunPerfil = false;
        Cursor c = getReadableDatabase().rawQuery("select count(*) from Perfil",null);
        c.moveToFirst();
        if(c.getInt(0) > 0)
            hayAlgunPerfil = true;
        c.close();
        close();
        return hayAlgunPerfil;
    }
	
	public boolean existePerfil(String perfil){
		SQLiteDatabase db = getReadableDatabase();
		String[] columns = {"Nombre"};
		String selectionArgs[] = {perfil};
		String whereClause = "Nombre=?";
		Cursor c = db.query("Perfil", columns, whereClause, selectionArgs, null, null, null);
		boolean success = false;
		if( c.getCount() > 0 )
			success = true;
		db.close();
		return success;
	}
	
	/*********
	 * 
	 * 
	 * PREGUNTA
	 * 
	 * 
	 * *****************/

	public boolean altaPregunta(String titulo, String votacion){
		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();
		Hasher hasher = new Hasher();
		values.put("ID", hasher.makeHash(titulo));
		values.put("Titulo", titulo);
		values.put("Votacion", hasher.makeHash(votacion));
		long result = db.insert("Pregunta","---",values);
		db.close();
        return result != -1;
	}

	public boolean quitarPregunta(String titulo){
		SQLiteDatabase db = getWritableDatabase();
		String whereClause = "Pregunta=?";
		String[] whereArgs = {new Hasher().makeHashString(titulo)};
		int count = db.delete("Pregunta", whereClause, whereArgs);
		db.close();
        return count > 0;
	}

	public boolean cambiaPregunta(String idPregunta, String nTitulo, String nVotacion){
		SQLiteDatabase db = getWritableDatabase();
		String whereClause = "ID=?";
		String whereArgs[] = {idPregunta};
		ContentValues values = new ContentValues();
		values.put("ID",new Hasher().makeHash(nTitulo));
		values.put("Titulo",nTitulo);
		values.put("Votacion", nVotacion);
        return db.update("Pregunta", values, whereClause, whereArgs) > 0;
	}

	public Pregunta[] obtenerPreguntas(){
		SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("select idVotacion from Votacion order by idVotacion desc limit 1", null);
        c.moveToFirst();
        int idVotacion = c.getInt(0);
        c.close();
        String[] columns = {"idPregunta","Pregunta"};
        String selection = "idVotacion = CAST(? as INTEGER)";
        String[] selectionArgs = {String.valueOf(idVotacion)};
		c = db.query("Pregunta", columns, selection, selectionArgs, null, null, null);
		List<Pregunta> preguntas = new ArrayList<>();
        Pregunta pregunta;
		while(c.moveToNext()) {
            pregunta = new Pregunta(c.getInt(c.getColumnIndex(columns[0])));
            pregunta.setEnunciado(c.getString(c.getColumnIndex(columns[1])));
            pregunta.setIdVotacion(idVotacion);
            preguntas.add(pregunta);
        }
		c.close();
        db.close();
		Pregunta[] arrayModel = new Pregunta[0];
		return preguntas.toArray(arrayModel);
	}
	
	public String obtenerID(String titulo){
		SQLiteDatabase db = getReadableDatabase();
		String[] columns = {"ID"};
		String selectionArgs[] = null;
		String whereClause = null;
		Cursor c = db.query("Pregunta", columns, whereClause, selectionArgs, null, null, null);
		String id = null;
		while(c.moveToNext())
			id = c.getString(c.getColumnIndex("ID"));
		db.close();
		return id;
	}
	
	/*********
	 * 
	 * 
	 * PREGUNTA OPCION
	 * 
	 * 
	 * *****************/

	public boolean agregarOpcionPregunta(String idPregunta, String idOpcion){
		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("Pregunta", idPregunta);
		values.put("Opcion", idOpcion);
		long result = db.insert("Pregunta_Opcion","---",values);
		db.close();
        return result != -1;
	}

	public boolean quitarPreguntaOpcion(String idPregunta, String idOpcion){
		SQLiteDatabase db = getWritableDatabase();
		String whereClause = "Pregunta=? and Opcion=?";
		String[] whereArgs = {idPregunta, idOpcion};
		int count = db.delete("Pregunta_Opcion", whereClause, whereArgs);
		db.close();
        return count > 0;
	}
	
	/*********
	 * 
	 * 
	 * URNA CONTENIDO
	 * 
	 * 
	 * *****************/

	public boolean aceptaVoto(int idUrna, String voto){
		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("Voto", voto);
		values.put("Urna", idUrna);
		long result = db.insert("UrnaContenido", "---", values);
		db.close();
        return result != -1;
	}

	public String[] obtenerVotos(){
		SQLiteDatabase db = getReadableDatabase();
		String[] columns = {"Voto"};
		String selectionArgs[] = null;
		String whereClause = null;
		Cursor c = db.query("UrnaContenido", columns, whereClause, selectionArgs, null, null, null);
		List<String> direcciones = new ArrayList<String>();
		while(c.moveToNext())
			direcciones.add(c.getString(c.getColumnIndex("Voto")));
		db.close();
		String[] resultado = new String[1];
		return direcciones.toArray(resultado);
	}
	
	/*********
	 * 
	 * 
	 * URNA HANDLER
	 * 
	 * 
	 * *****************/

	public boolean altaUrna(String votacion, String tag){
		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("Votacion", new Hasher().makeHash(votacion));
		values.put("Tag", tag);
		long result = db.insert("Urna","---",values);
		db.close();
        return result != -1;
	}

	public boolean quitarUrna(int id){
		SQLiteDatabase db = getWritableDatabase();
		String whereClause = "ID=?";
		String[] whereArgs = {Integer.valueOf(id).toString()};
		int count = db.delete("Urna", whereClause, whereArgs);
		db.close();
        return count > 0;
	}

	public boolean cambiaUrna(int id, String nuevoTag, String nuevaVotacionReferida){
		SQLiteDatabase db = getWritableDatabase();
		String whereClause = "ID=?";
		String whereArgs[] = {Integer.valueOf(id).toString()};
		ContentValues values = new ContentValues();
		values.put("Votacion",nuevaVotacionReferida);
		values.put("Tag", nuevoTag);
        return db.update("Urna", values, whereClause, whereArgs) > 0;
	}

	public String[] obtenerUrnas(){
		SQLiteDatabase db = getReadableDatabase();
		String[] columns = {"ID","Votacion","Tag"};
		String selectionArgs[] = null;
		String whereClause = null;
		Cursor c = db.query("Urna", columns, whereClause, selectionArgs, null, null, null);
		List<String> direcciones = new ArrayList<String>();
		while(c.moveToNext())
			direcciones.add(c.getString(c.getColumnIndex("ID")) + "," + c.getString(c.getColumnIndex("Votacion")) + "," + c.getString(c.getColumnIndex("Tag")));
		db.close();
		String[] resultado = new String[1];
		return direcciones.toArray(resultado);
	}
	
	/*********
	 * 
	 * 
	 * VOTACION
	 * 
	 * 
	 * *****************/


	public boolean altaVotacion(String titulo, String fechaInicio, String fechaFin){
		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("ID", new Hasher().makeHash(titulo));
		values.put("Titulo", titulo);
		values.put("Fecha_Inicio", fechaInicio);
		values.put("Fecha_Fin", fechaFin);
		long result = db.insert("Votacion","---",values);
		db.close();
        return result != -1;
	}

	public boolean quitarVotacion(String titulo){
		SQLiteDatabase db = getWritableDatabase();
		String whereClause = "ID=?";
		String[] whereArgs = {new Hasher().makeHashString(titulo)};
		int count = db.delete("Votacion", whereClause, whereArgs);
		db.close();
        return count > 0;
	}

	public boolean cambiaVotacion(String idVotacion, String nTitulo, String nFechaInicio, String nFechaFin){
		SQLiteDatabase db = getWritableDatabase();
		String whereClause = "ID=?";
		String whereArgs[] = {idVotacion};
		ContentValues values = new ContentValues();
		values.put("Titulo",nTitulo);
		values.put("Fecha_Inicio",nFechaInicio);
		values.put("Fecha_Fin", nFechaFin);
        return db.update("Votacion", values, whereClause, whereArgs) > 0;
	}

	public String obteneVotacion(String titulo){
		SQLiteDatabase db = getReadableDatabase();
		String[] columns = {"ID","Titulo", "Fecha_Inicio", "Fecha_Fin"};
		String selectionArgs[] = {titulo};
		String whereClause = "Titulo=?";
		Cursor c = db.query("Votacion", columns, whereClause, selectionArgs, null, null, null);
		List<String> direcciones = new ArrayList<String>();
		while(c.moveToNext())
			direcciones.add(c.getString(c.getColumnIndex("ID")) + "," + c.getString(c.getColumnIndex("Titulo")) + "," + c.getString(c.getColumnIndex("Fecha_Inicio")) + "," + c.getString(c.getColumnIndex("Fecha_Fin")));
		db.close();
		return direcciones.get(0);
	}

	public String[] obteneVotaciones(){
		SQLiteDatabase db = getReadableDatabase();
		String[] columns = {"ID","Titulo", "Fecha_Inicio", "Fecha_Fin"};
		String selectionArgs[] = null;
		String whereClause = null;
		Cursor c = db.query("Votacion", columns, whereClause, selectionArgs, null, null, null);
		List<String> direcciones = new ArrayList<String>();
		while(c.moveToNext())
			direcciones.add(c.getString(c.getColumnIndex("ID")) + "," + c.getString(c.getColumnIndex("Titulo")) + "," + c.getString(c.getColumnIndex("Fecha_Inicio")) + "," + c.getString(c.getColumnIndex("Fecha_Fin")));
		db.close();
		String[] resultado = new String[1];
		return direcciones.toArray(resultado);
	}
	
	/*********
	 * 
	 * 
	 * ZONA VOTACION
	 * 
	 * 
	 * *****************/


	public boolean altaZonaVoto(String direccion, float latitud, float longitud){
		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("ID", new Hasher().makeHash(direccion));
		values.put("Direccion", direccion);
		values.put("Latitud", latitud);
		values.put("Longitud", longitud);
		long result = db.insert("Zona","---",values);
		db.close();
        return result != -1;
	}
	
	public boolean quitarZona(String direccion){
		SQLiteDatabase db = getWritableDatabase();
		String whereClause = "ID=?";
		String[] whereArgs = {new Hasher().makeHashString(direccion)};
		int count = db.delete("Zona", whereClause, whereArgs);
		db.close();
        return count > 0;
	}
	
	public boolean cambiaZona(String antiguaDireccion, String nuevaDireccion, float latitud, float longitud){
		SQLiteDatabase db = getWritableDatabase();
		Hasher hasher = new Hasher();
		String antiguoID = hasher.makeHashString(antiguaDireccion);
		String nuevoID = hasher.makeHashString(antiguaDireccion);
		String whereClause = "ID=?";
		String whereArgs[] = {antiguoID};
		ContentValues values = new ContentValues();
		values.put("ID", nuevoID);
		values.put("Direccion",nuevaDireccion);
		values.put("Latitud",latitud);
		values.put("Longitud", longitud);
        return db.update("Zona", values, whereClause, whereArgs) > 0;
	}
	
	public String[] obtenerZonas(){
		SQLiteDatabase db = getReadableDatabase();
		String[] columns = {"Direccion"};
		String selectionArgs[] = null;
		String whereClause = null;
		Cursor c = db.query("Zona", columns, whereClause, selectionArgs, null, null, null);
		List<String> direcciones = new ArrayList<String>();
		while(c.moveToNext())
			direcciones.add(c.getString(c.getColumnIndex("Direccion")));
		db.close();
		String[] resultado = new String[1];
		return direcciones.toArray(resultado);
	}

    public void quitarEscuela(String elemento) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete("Escuela","Nombre = ?",new String[]{elemento});
    }

    public void guardarDatosDeAgente(String mainHost, String secHost, int hbPeriod, String members, int idVotacion){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        /***********************************************************
         *
         * DatosDeAgenteDeVotacion(
         *  id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
         *  MainHost TEXT NOT NULL,
         *  SecondaryHost TEXT NOT NULL,
         *  HBPeriod INTEGER DEFAULT 30,
         *  Participantes TEXT NOT NULL,
         *  idVotacion INTEGER NOT NULL,
         *  FOREIGN KEY(idVotacion) REFERENCES Votacion(idVotacion)
         * );
         *
         ********************************************************************/
        values.put("MainHost", mainHost);
        values.put("SecondaryHost", secHost);
        values.put("HBPeriod",hbPeriod);
        values.put("Participantes", members);
        values.put("idVotacion", idVotacion);
        db.insert("DatosDeAgenteDeVotacion", "---", values);
        db.close();
    }

    public DatosAgenteDeInteraccion leeDatosDeAgente(int idVotacion){
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("select * from DatosDeAgenteDeVotacion where idVotacion = " +
                "CAST(? as INTEGER)", new String[]{String.valueOf(idVotacion)});
        if(c.moveToNext()){
            DatosAgenteDeInteraccion datos = new DatosAgenteDeInteraccion();
            datos.setMainHost(c.getString(c.getColumnIndex("MainHost")));
            datos.setSecondaryHost(c.getString(c.getColumnIndex("SecondaryHost")));
            datos.setHbPeriod(c.getInt(c.getColumnIndex("HBPeriod")));
            try{datos.setMembers(new JSONArray(c.getString(c.getColumnIndex("Participantes"))));}catch(JSONException e){e.printStackTrace();}
            datos.setIdVotacion(idVotacion);
            c.close();
            db.close();
            return datos;
        }else{
            c.close();
            db.close();
            return null;
        }
    }

    public int obtenerUltimaVotacionGlobal() {
        SQLiteDatabase db;
        Cursor c;
        int idVotacion;
        int idVotacionGlobal;
        db = getReadableDatabase();
        c = db.rawQuery("select idVotacion from Votacion order by idVotacion DESC limit 1", null);
        c.moveToFirst();
        idVotacion = c.getInt(0);
        c.close();
        c = db.rawQuery("select idVotacionGlobal from VotacionGlobal where idVotacion = CAST(? as INTEGER)",
                new String[]{String.valueOf(idVotacion)});
        if(c.moveToFirst()){
            idVotacionGlobal = c.getInt(0);
        }else{
            idVotacionGlobal = -1;
        }
        c.close();
        db.close();
        return idVotacionGlobal;
    }

    public void finalizaVotacion(int idVotacion) {
        SQLiteDatabase db;
        ContentValues values;
        db = getWritableDatabase();
        values = new ContentValues();
        values.put("idVotacion", idVotacion);
        values.put("concluida", 1);
        db.update("Votacion", values, "idVotacion = CAST(? as INTEGER)", new String[]{String.valueOf(idVotacion)});
        db.close();
    }

    public void guardarHashVotacion(int idVotacion, String hash){
        ContentValues values = new ContentValues();
        values.put("idVotacion", idVotacion);
        values.put("Hash", hash);
        SQLiteDatabase db = getWritableDatabase();
        db.insert("HashVotacion", "---", values);
        db.close();
    }

    public String obtenerHashVotacion(int idVotacion){
        SQLiteDatabase db;
        Cursor c;
        String hash = null;
        db = getReadableDatabase();
        c = db.rawQuery("select Hash from HashVotacion where idVotacion = CAST(? as INTEGER)"
                ,new String[]{String.valueOf(idVotacion)});
        if(c.moveToFirst()){
            hash = c.getString(0);
        }
        c.close();
        db.close();
        return hash;
    }
}