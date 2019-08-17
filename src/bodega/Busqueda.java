package bodega;
import java.sql.*;
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
    
    @Override 
    public void run(){
        busqueda();
    }
    
    public void busqueda(){
        login = new ConectorSesion();
        Connection cn = login.getConnection();
        int firstId = 0;
        int lastId = 0;
        String destinoFinal = "";
        String estadoActual = "";
        String bodega = "";
        String primero = "SELECT * FROM Rutas LIMIT 1";
        String ultimo = "SELECT * FROM Rutas ORDER BY id DESC LIMIT 1";
        String destinoBodega = "SELECT * FROM Bodega WHERE destino = ?";
        String contadorBodega = "SELECT COUNT(*) FROM Bodega WHERE prioridad = 'SI' AND destino = ?";    
        String contadorBodega2 = "SELECT COUNT(*) FROM Bodega WHERE prioridad = 'NO' AND destino = ?";
        String busqueda = "SELECT * FROM Bodega WHERE prioridad = 'SI' AND destino = ? LIMIT ?";
        String busqueda2 = "SELECT * FROM Bodega WHERE prioridad = 'NO'AND destino = ? LIMIT ?";
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
                                    if(total>cont2){
                                        for(int x = 1; x<=cont2; x++){
                                            PreparedStatement declaracionBusqueda2 = cn.prepareStatement(busqueda2);
                                            declaracionBusqueda2.setString(1, bodega);
                                            declaracionBusqueda2.setInt(2, x);
                                            ResultSet result9 = declaracionBusqueda2.executeQuery();
                                            while(result9.next()){
                                                nit2 = result9.getInt("nit_persona");
                                                noVenta = result9.getInt("no_venta");
                                                noPaquete = result9.getInt("no_paquete_venta");
                                            }
                                            actualizacion = rango + cont2;
                                            //Actualiza el valor de los paquetes actuales en los puntos de control
                                            PreparedStatement decActualizacionPControl2 = cn.prepareStatement(actualizarRuta);
                                            decActualizacionPControl2.setInt(1, actualizacion);
                                            decActualizacionPControl2.execute();
                                            //Agrega los paquetes a sus puntos en especifico cuando vea un espacio
                                            String agregarPunto = "INSERT INTO Paquetes VALUES ('"+0+"','"+nit2+"','"+1+"','"+i+"','"+noVenta+"','"+noPaquete+"')";
                                            PreparedStatement decActPaquete2 = cn.prepareStatement(agregarPunto);
                                            decActPaquete2.execute();
                                            //Saca los paqutes de la bodega
                                            PreparedStatement decVacioBodega2 = cn.prepareStatement(quitarBodega2);
                                            decVacioBodega2.setString(1, bodega);
                                            decVacioBodega2.setInt(2, x);
                                            decVacioBodega2.execute();
                                        }
                                    } else if (cont2>total){
                                        for(int x = 1; x<=total; x++){
                                            PreparedStatement declaracionBusqueda2 = cn.prepareStatement(busqueda2);
                                            declaracionBusqueda2.setString(1, bodega);
                                            declaracionBusqueda2.setInt(2, x);
                                            ResultSet result10 = declaracionBusqueda2.executeQuery();
                                            while(result10.next()){
                                                nit2 = result10.getInt("nit_persona");
                                                noVenta = result10.getInt("no_venta");
                                                noPaquete = result10.getInt("no_paquete_venta");
                                            
                                            }
                                            actualizacion = rango + total;
                                            //Actualiza el valor de los paquetes actuales en los puntos de control
                                            PreparedStatement decActualizacionPControl2 = cn.prepareStatement(actualizarRuta);
                                            decActualizacionPControl2.setInt(1, actualizacion);
                                            decActualizacionPControl2.execute();
                                            //Agrega los paquetes a sus puntows en especifico cuando vea un espacio
                                            String agregarPunto = "INSERT INTO Paquetes VALUES ('"+0+"','"+nit2+"','"+1+"','"+i+"','"+noVenta+"','"+noPaquete+"')";
                                            PreparedStatement decActPaquete2 = cn.prepareStatement(agregarPunto);
                                            decActPaquete2.execute();
                                            //Saca los paquetes de la bodega
                                            PreparedStatement decVacioBodega2 = cn.prepareStatement(quitarBodega2);
                                            decVacioBodega2.setString(1, bodega);
                                            decVacioBodega2.setInt(2, x);
                                            decVacioBodega2.execute();
                                        }
                                    } else if(total == 0){
                                        System.out.println("El punto de control se encuentra lleno por el momento");
                                    }       
                                }                                
                            } else {                            
                                if(total>cont){
                                    for(int x = 1; x<=cont; x++){
                                        PreparedStatement declaracionBusqueda = cn.prepareStatement(busqueda);
                                        declaracionBusqueda.setString(1, bodega);
                                        declaracionBusqueda.setInt(2, x);     
                                        ResultSet result7 = declaracionBusqueda.executeQuery();
                                        while(result7.next()){
                                            nitU = result7.getInt("nit_persona");
                                            noVent = result7.getInt("no_venta");
                                            noPaquet = result7.getInt("no_paquete_venta");
                                        }
                                        actualizacion = rango + cont;
                                        //Actualiza el valor de los paquetes actuales en los puntos de control
                                        PreparedStatement decActualizacionPControl = cn.prepareStatement(actualizarRuta);
                                        decActualizacionPControl.setInt(1, actualizacion);
                                        decActualizacionPControl.execute();
                                        //Agrega los paquetes a sus puntos en especifico cuando vea un espacio
                                        String agregarPunto = "INSERT INTO Paquetes VALUES ('"+0+"','"+nitU+"','"+1+"','"+i+"','"+noVent+"','"+noPaquet+"')";
                                        PreparedStatement decActPaquete = cn.prepareStatement(agregarPunto);
                                        decActPaquete.execute(); 
                                        //Saca los paquetes de la bodega
                                        PreparedStatement decVacioBodega = cn.prepareStatement(quitarBodega);
                                        decVacioBodega.setString(1, bodega);
                                        decVacioBodega.setInt(2, x);
                                        decVacioBodega.execute();            
                                    }
                                } else if(cont>total){
                                    for(int x = 1; x<=total; x++){
                                        PreparedStatement declaracionBusqueda = cn.prepareStatement(busqueda);
                                        declaracionBusqueda.setString(1, bodega);
                                        declaracionBusqueda.setInt(2, x);
                                        ResultSet result7 = declaracionBusqueda.executeQuery();
                                        while(result7.next()){
                                            nitU = result7.getInt("nit_persona");
                                            noVent = result7.getInt("no_venta");
                                            noPaquet = result7.getInt("no_paquete_venta");
                                       
                                        }
                                        actualizacion = rango + total;
                                        //Actualiza el valor de los paquetes actuales en los puntos de control
                                        PreparedStatement decActualizacionPControl = cn.prepareStatement(actualizarRuta);
                                        decActualizacionPControl.setInt(1, actualizacion);
                                        decActualizacionPControl.execute();
                                        //Agrega los paquetes a sus puntos en especifico cuando vea un espacio
                                        String agregarPunto = "INSERT INTO Paquetes VALUES ('"+0+"','"+nitU+"','"+1+"','"+i+"','"+noVent+"','"+noPaquet+"')";
                                        PreparedStatement decActPaquete = cn.prepareStatement(agregarPunto);
                                        decActPaquete.execute();
                                        //Saca los paquetes de la bodega
                                        PreparedStatement decVacioBodega = cn.prepareStatement(quitarBodega); 
                                        decVacioBodega.setString(1, bodega);
                                        decVacioBodega.setInt(2, x);
                                        decVacioBodega.execute();
                                    }
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
}