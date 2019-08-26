package bodega;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author luisGonzalez
 */
public class Busqueda extends TimerTask {
     
    private ConectorSesion login;
    private int valor = 0;
    private int rango = 0;
    private int total;  
    private int actualizacion;
    private Date myDate = new Date();
    
    @Override 
    public void run(){
        busqueda();
    }
    
    public void busqueda(){
        login = new ConectorSesion();
        Connection cn = login.getConnection();
        String fecha = new SimpleDateFormat("dd-MM-yyyy").format(myDate);
        int firstId = 0;
        int lastId = 0;
        //int ingresosRuta = 0;
        int paquetesTotales = 0;
        int ingresosRuta = 0;
        String destinoFinal = "";
        String estadoActual = "";
        int paquetesActRuta = 0;
        String bodega = "";
        String primero = "SELECT * FROM Rutas LIMIT 1";
        String ultimo = "SELECT * FROM Rutas ORDER BY id DESC LIMIT 1";
        String destinoBodega = "SELECT * FROM Bodega WHERE destino = ?";
        String contadorBodega = "SELECT COUNT(*) FROM Bodega WHERE prioridad = 'SI' AND destino = ?";    
        String contadorBodega2 = "SELECT COUNT(*) FROM Bodega WHERE prioridad = 'NO' AND destino = ?";
        String busqueda = "SELECT * FROM Bodega WHERE prioridad = 'SI' AND destino = ? LIMIT ?";
        String busqueda2 = "SELECT * FROM Bodega WHERE prioridad = 'NO' AND destino = ? LIMIT ?";
        //-------------------------------------------------------------------------------------------------
        String quitarBodega = "DELETE FROM Bodega WHERE prioridad = 'SI' AND destino = ? LIMIT ?";
        String quitarBodega2 = "DELETE FROM Bodega WHERE prioridad = 'NO' AND destino = ? LIMIT ?";
        try {
            PreparedStatement declaracion = cn.prepareStatement(primero);
            PreparedStatement declaracion2 = cn.prepareStatement(ultimo);
            ResultSet result = declaracion.executeQuery();
            ResultSet result2 = declaracion2.executeQuery();
            while(result.next()){
                firstId = result.getInt("id");
            }
            while(result2.next()){
                lastId = result2.getInt("id");
            }
            for(int i = firstId; i<=lastId; i++){
                String destino = "SELECT * FROM Rutas WHERE id = ?"; 
                String busquedaPControl = "SELECT * FROM Puntos_control_ruta_"+i+" LIMIT 1";
                //-----------------------------------------------------------------------------------------
                String actualizarRuta = "UPDATE Puntos_control_ruta_"+i+" SET paquetes_actuales = ? LIMIT 1"; 
                
                PreparedStatement declaracionPControl = cn.prepareStatement(busquedaPControl);
                ResultSet result3 = declaracionPControl.executeQuery();
                PreparedStatement declaracionDestino = cn.prepareStatement(destino);
                declaracionDestino.setInt(1, i);
                ResultSet result4 = declaracionDestino.executeQuery();
                while(result4.next()){
                    destinoFinal = result4.getString("destino");
                    estadoActual = result4.getString("estado");
                    paquetesActRuta = result4.getInt("paquetes_en_sistema");
                    paquetesTotales = result4.getInt("paquetes_totales");
                                    
                }
                if(estadoActual.equals("ACTIVADA")){   
                    PreparedStatement declaracionBodega = cn.prepareStatement(destinoBodega);
                    declaracionBodega.setString(1, destinoFinal);
                    ResultSet result5 = declaracionBodega.executeQuery();
                    while(result3.next()){
                        valor = result3.getInt("paquetes_maximos");
                        rango = result3.getInt("paquetes_actuales");
                    }
                    total = valor - rango;
                    System.out.println("Quedan "+total+" espacios disponibles en el punto de control 1 de la ruta "+i+" con destino hacia "+destinoFinal+" estado: "+estadoActual);
                
                    if(result5.next()){
                        bodega = result5.getString("destino");
                        System.out.println("hay coincidencia");
                        PreparedStatement declaracionContador = cn.prepareStatement(contadorBodega);
                        declaracionContador.setString(1, bodega);
                        ResultSet result6 = declaracionContador.executeQuery();
                        while(result6.next()){
                            int cont = 0;
                            int nitU = 0;
                            int noVent = 0;
                            int noPaquet = 0;
                            int[] precioPaquete;
                            cont = result6.getInt("COUNT(*)");
                                
                            //SI EL CONTADOR DE PRIORIDAD 'SI' ES 0 ENTONCES SE PROCEDE A ENVIAR LOS PAQUETES QUE NO TIENEN PRIORIDAD
                            if(cont == 0){
                                PreparedStatement declaracionContador2 = cn.prepareStatement(contadorBodega2);
                                declaracionContador2.setString(1, bodega);
                                ResultSet result8 = declaracionContador2.executeQuery();
                                while(result8.next()){
                                    int cont2 = 0;
                                    int nit2 = 0;
                                    int noVenta = 0;
                                    int noPaquete = 0;
                                    cont2 = result8.getInt("COUNT(*)");
                                    if(total>=cont2){
                                        precioPaquete = new int[cont2+1];
                                    
                                        for(int x = 1; x<=cont2; x++){
                                            PreparedStatement declaracionBusqueda2 = cn.prepareStatement(busqueda2);
                                            declaracionBusqueda2.setString(1, bodega);
                                            declaracionBusqueda2.setInt(2, x);
                                            ResultSet result9 = declaracionBusqueda2.executeQuery();
                                            while(result9.next()){
                                                nit2 = result9.getInt("nit_persona");
                                                noVenta = result9.getInt("no_venta");
                                                noPaquete = result9.getInt("no_paquete_venta");
                                                precioPaquete[x] = result9.getInt("costo_paquete");
                                            }
                                            actualizacion = rango + cont2;
                                            //agrega el ingreso en efectivo a la ruta a la que el paquete sea enviado
                                            revisarRuta(cn, i, ingresosRuta, precioPaquete, x);
                                            //Actualiza el valor de los paquetes actuales en la ruta
                                            paquetesSistema(cn, i, paquetesActRuta, cont2);
                                            paquetesTotales(cn, i, paquetesTotales, cont2);
                                            //Actualiza el valor de los paquetes actuales en los puntos de control
                                            PreparedStatement decActualizacionPControl2 = cn.prepareStatement(actualizarRuta);
                                            decActualizacionPControl2.setInt(1, actualizacion);
                                            decActualizacionPControl2.execute();
                                            //Agrega los paquetes a sus puntos en especifico cuando vea un espacio
                                            String agregarPunto = "INSERT INTO Paquetes VALUES ('"+0+"','"+nit2+"','"+1+"','"+i+"','"+noVenta+"','"+noPaquete+"','"+precioPaquete[x]+"')";
                                            PreparedStatement decActPaquete2 = cn.prepareStatement(agregarPunto);
                                            decActPaquete2.execute();
                                            //Crea el registro en horas de un paquete
                                            String registroHoras = "INSERT INTO Registro_horas VALUES ('"+0+"','"+nit2+"','"+i+"','"+0+"','"+noVenta+"','"+noPaquete+"')";
                                            PreparedStatement declaracionRegistro = cn.prepareStatement(registroHoras);
                                            declaracionRegistro.execute();
                                            //Agrega el registro a tabla fechas la cual es permamente
                                            agregarFecha(cn, nit2, i, noVenta, noPaquete, fecha);
                                        }
                                        //Saca los paquetes de la bodega
                                        PreparedStatement decVacioBodega2 = cn.prepareStatement(quitarBodega2);
                                        decVacioBodega2.setString(1, bodega);
                                        decVacioBodega2.setInt(2, cont2);
                                        decVacioBodega2.execute();
                                    } else if (cont2>total){
                                         precioPaquete = new int[total+1];
                                   
                                        for(int x = 1; x<=total; x++){
                                            PreparedStatement declaracionBusqueda2 = cn.prepareStatement(busqueda2);
                                            declaracionBusqueda2.setString(1, bodega);
                                            declaracionBusqueda2.setInt(2, x);
                                            ResultSet result10 = declaracionBusqueda2.executeQuery();
                                            while(result10.next()){
                                                nit2 = result10.getInt("nit_persona");
                                                noVenta = result10.getInt("no_venta");
                                                noPaquete = result10.getInt("no_paquete_venta"); 
                                                precioPaquete[x] = result10.getInt("costo_paquete");
                                            }
                                            actualizacion = rango + total;
                                            //agrega el ingreso en efectivo a la ruta a la que el paquete sea enviado
                                            revisarRuta(cn, i, ingresosRuta, precioPaquete, x);
                                            //Actualiza el valor de los paquetes actuales en la ruta
                                            paquetesSistema(cn, i, paquetesActRuta, total);
                                            paquetesTotales(cn, i, paquetesTotales, total);
                                            //Actualiza el valor de los paquetes actuales en los puntos de control
                                            PreparedStatement decActualizacionPControl2 = cn.prepareStatement(actualizarRuta);
                                            decActualizacionPControl2.setInt(1, actualizacion);
                                            decActualizacionPControl2.execute();
                                            //Agrega los paquetes a sus puntows en especifico cuando vea un espacio
                                            String agregarPunto = "INSERT INTO Paquetes VALUES ('"+0+"','"+nit2+"','"+1+"','"+i+"','"+noVenta+"','"+noPaquete+"','"+precioPaquete[x]+"')";
                                            PreparedStatement decActPaquete2 = cn.prepareStatement(agregarPunto);
                                            decActPaquete2.execute();
                                            //Crea el registro en horas de un paquete
                                            String registroHoras = "INSERT INTO Registro_horas VALUES ('"+0+"','"+nit2+"','"+i+"','"+0+"','"+noVenta+"','"+noPaquete+"')";
                                            PreparedStatement declaracionRegistro = cn.prepareStatement(registroHoras);
                                            declaracionRegistro.execute();
                                            //Agrega el registro a tabla fechas la cual es permamente
                                            agregarFecha(cn, nit2, i, noVenta, noPaquete, fecha);
                                        }
                                        //Saca los paquetes de la bodega
                                        PreparedStatement decVacioBodega2 = cn.prepareStatement(quitarBodega2);
                                        decVacioBodega2.setString(1, bodega);
                                        decVacioBodega2.setInt(2, total);
                                        decVacioBodega2.execute();
                                    } else if(total == 0){
                                        System.out.println("El punto de control se encuentra lleno por el momento");
                                    }       
                                }                                
                            } else {                            
                                if(total>=cont){
                                     precioPaquete = new int[cont+1];
                                   
                                    for(int x = 1; x<=cont; x++){
                                        PreparedStatement declaracionBusqueda = cn.prepareStatement(busqueda);
                                        declaracionBusqueda.setString(1, bodega);
                                        declaracionBusqueda.setInt(2, x);     
                                        ResultSet result7 = declaracionBusqueda.executeQuery();
                                        while(result7.next()){
                                            nitU = result7.getInt("nit_persona");
                                            noVent = result7.getInt("no_venta");
                                            noPaquet = result7.getInt("no_paquete_venta");
                                            precioPaquete[x] = result7.getInt("costo_paquete");
                                        }
                                        actualizacion = rango + cont;
                                        //agrega el ingreso en efectivo a la ruta a la que el paquete sea enviado
                                        revisarRuta(cn, i, ingresosRuta, precioPaquete, x);
                                        //Actualiza el valor de los paquetes actuales en la ruta
                                        paquetesSistema(cn, i, paquetesActRuta, cont);
                                        paquetesTotales(cn, i, paquetesTotales, cont);
                                        //Actualiza el valor de los paquetes actuales en los puntos de control
                                        PreparedStatement decActualizacionPControl = cn.prepareStatement(actualizarRuta);
                                        decActualizacionPControl.setInt(1, actualizacion);
                                        decActualizacionPControl.execute();
                                        //Agrega los paquetes a sus puntos en especifico cuando vea un espacio
                                        String agregarPunto = "INSERT INTO Paquetes VALUES ('"+0+"','"+nitU+"','"+1+"','"+i+"','"+noVent+"','"+noPaquet+"','"+precioPaquete[x]+"')";
                                        PreparedStatement decActPaquete = cn.prepareStatement(agregarPunto);
                                        decActPaquete.execute();     
                                        //Crea el registro en horas de un paquete
                                        String registroHoras = "INSERT INTO Registro_horas VALUES ('"+0+"','"+nitU+"','"+i+"','"+0+"','"+noVent+"','"+noPaquet+"')";
                                        PreparedStatement declaracionRegistro = cn.prepareStatement(registroHoras);
                                        declaracionRegistro.execute();                            
                                        //Agrega el registro a tabla fechas la cual es permamente
                                        agregarFecha(cn, nitU, i, noVent, noPaquet, fecha);
                                    }
                                    //Saca los paquetes de la bodega
                                    PreparedStatement decVacioBodega2 = cn.prepareStatement(quitarBodega);
                                    decVacioBodega2.setString(1, bodega);
                                    decVacioBodega2.setInt(2, cont);
                                    decVacioBodega2.execute();
                                } else if(cont>total){
                                     precioPaquete = new int[total+1];
                                   
                                    for(int x = 1; x<=total; x++){
                                        PreparedStatement declaracionBusqueda = cn.prepareStatement(busqueda);
                                        declaracionBusqueda.setString(1, bodega);
                                        declaracionBusqueda.setInt(2, x);
                                        ResultSet result7 = declaracionBusqueda.executeQuery();
                                        while(result7.next()){
                                            nitU = result7.getInt("nit_persona");
                                            noVent = result7.getInt("no_venta");
                                            noPaquet = result7.getInt("no_paquete_venta");
                                            precioPaquete[x] = result7.getInt("costo_paquete");
                                        }
                                        actualizacion = rango + total;
                                        //agrega el ingreso en efectivo a la ruta a la que el paquete sea enviado
                                        revisarRuta(cn, i, ingresosRuta, precioPaquete, x);
                                        //Actualiza el valor de los paquetes actuales en la ruta
                                        paquetesSistema(cn, i, paquetesActRuta, total); 
                                        paquetesTotales(cn, i, paquetesTotales, total);
                                        //Actualiza el valor de los paquetes actuales en los puntos de control
                                        PreparedStatement decActualizacionPControl = cn.prepareStatement(actualizarRuta);
                                        decActualizacionPControl.setInt(1, actualizacion);
                                        decActualizacionPControl.execute();
                                        //Agrega los paquetes a sus puntos en especifico cuando vea un espacio
                                        String agregarPunto = "INSERT INTO Paquetes VALUES ('"+0+"','"+nitU+"','"+1+"','"+i+"','"+noVent+"','"+noPaquet+"','"+precioPaquete[x]+"')";
                                        PreparedStatement decActPaquete = cn.prepareStatement(agregarPunto);
                                        decActPaquete.execute();   
                                        //Crea el registro en horas de un paquete
                                        String registroHoras = "INSERT INTO Registro_horas VALUES ('"+0+"','"+nitU+"','"+i+"','"+0+"','"+noVent+"','"+noPaquet+"')";
                                        PreparedStatement declaracionRegistro = cn.prepareStatement(registroHoras);
                                        declaracionRegistro.execute();
                                        //Agrega el registro a tabla fechas la cual es permamente
                                        agregarFecha(cn, nitU, i, noVent, noPaquet, fecha);
                                    }
                                    //Saca los paquetes de la bodega
                                    PreparedStatement decVacioBodega2 = cn.prepareStatement(quitarBodega);
                                    decVacioBodega2.setString(1, bodega);
                                    decVacioBodega2.setInt(2, total);
                                    decVacioBodega2.execute();
                                } else if(total == 0){
                                    System.out.println("El punto de control se encuentra lleno por completo");
                                }
                            }
                        }                    
                    } else {
                        System.out.println("NO hay coincidencia");
                    }
                } else {
                    System.out.println("La ruta "+i+" con destino hacia "+destinoFinal+" se encuentra desactivada por lo que no se puede trabajar en ella por el momento");
                }
                
            }
        } catch (SQLException ex) {
            Logger.getLogger(Busqueda.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            login.Desconectar();
        }
    }
    
    private void paquetesSistema(Connection cn, int i, int paquetesActRuta, int nuevoDato) throws SQLException {
        int nuevoValorRuta;
        nuevoValorRuta = paquetesActRuta + nuevoDato;
        String sql = "UPDATE Rutas SET paquetes_en_sistema = ? WHERE id = ?";
        PreparedStatement declaracionRuta = cn.prepareStatement(sql);
        declaracionRuta.setInt(1, nuevoValorRuta);
        declaracionRuta.setInt(2, i);
        declaracionRuta.execute(); 
    }
    
    private void paquetesTotales(Connection cn, int i, int paquetesTotales, int nuevoDato) throws SQLException{
        int nuevoValor;
        nuevoValor = paquetesTotales + nuevoDato;
        String sql = "UPDATE Rutas SET paquetes_totales = ? WHERE id = ?";
        PreparedStatement declaracionRuta = cn.prepareStatement(sql);
        declaracionRuta.setInt(1, nuevoValor);
        declaracionRuta.setInt(2, i);
        declaracionRuta.execute();
    }
    
    private void agregarFecha(Connection cn, int nit, int i, int noVenta, int noPaquete, String fecha) throws SQLException{
        String registroFecha = "INSERT INTO Registro_fechas VALUES ('"+0+"','"+nit+"','"+i+"','"+noVenta+"','"+noPaquete+"','"+fecha+"')";
        PreparedStatement declaracionFecha = cn.prepareStatement(registroFecha);
        declaracionFecha.execute();
                                            
    }
    
    private void revisarRuta(Connection cn, int i, int ingresosRuta, int[] precioPaquete, int x) throws SQLException{
        int nuevoValor, costosRuta, ganancias; 
        String ingresos = "UPDATE Rutas SET ingresos_ruta = ? WHERE id = ?";
        String destino = "SELECT * FROM Rutas WHERE id = ?"; 
        String gananciasRuta = "UPDATE Rutas SET ganancias_totales = ? WHERE id = ?";
        PreparedStatement declaracionRuta = cn.prepareStatement(destino);
        declaracionRuta.setInt(1, i);
        ResultSet result = declaracionRuta.executeQuery();
        while(result.next()){
            ingresosRuta = result.getInt("ingresos_ruta");
            costosRuta = result.getInt("costos_ruta");
            nuevoValor = ingresosRuta + precioPaquete[x];
            ganancias = nuevoValor - costosRuta;
            PreparedStatement declaracionIngresos = cn.prepareStatement(ingresos);
            declaracionIngresos.setInt(1, nuevoValor);
            declaracionIngresos.setInt(2, i);
            declaracionIngresos.execute();   
            PreparedStatement declaracionGanancias = cn.prepareStatement(gananciasRuta);
            declaracionGanancias.setInt(1, ganancias);
            declaracionGanancias.setInt(2, i);
            declaracionGanancias.execute();
        }
    }
}