package com.example.slimmx

data class ResponseMessage(
    val status: Int,
    val message:String,
    val data:String
)

data class ListaLogin(
    val token: String,
    val roles: List<roles>,
    val username: String,
    val device_type:String
)

data class roles(
    val rolname:String
)

data class ResultadoJsonSlimFolios_Packing(
    val ID: String,
    val ITEMS: Double
)

data class ListaItem(
    val ID: String,
    val CODIGO: String,
    val DESCRIPCION: String,
    val CONFIRMADA: Int,
    val EMPACADA: Int,
    val POR_EMPACAR: Int,
    val REFERENCIA: String
)

data class ListaCaja(
    val CAJA: Int,
    val EMPAQUES: Int
)

data class ListaCajaItems(
    val ID: String,
    val EMPAQUE: Int,
    val CODIGO: String,
    val DESCRIPCION: String,
    val CANTIDAD: Int,
    val FECHA_HORA: String,
    val CAJA: Int
)

data class ListaConfirmacionEmpaque(
    val picking_items_empaque: PickingItemsEmpaque
)

data class PickingItemsEmpaque(
    val CODE: Int,
    var MESSAGE: String
)

data class ListaCajaConfirmacion(
    val status: String,
    val message:String,
    val result: ListaCerrarCaja
)

data class ListaCerrarCaja(
    val CODE: Int,
    val MESSAGE: String,
    val BOX: Int,
    val CANTIDAD: Int,
    val FECHA_HORA: String
)

data class ListaFolioPickingEnvio(
    val ID:String,
    val TOTAL:Int
)

data class ListaItemsPi_Envio(
    val ALMACEN_ID: Int,
    val AREA_ID: Int,
    val UBICACION_ORIGEN: String,
    val BOX_ORIGEN:String,
    val UBICACION_DESTINO:String,
    val BOX_DESTINO:String,
    val CANTIDAD:Int,
    val CODIGO:String,
    val DESCRIPCION:String,
    val ITEM:Int,
    val CANTIDAD_CONFIRMADA:Int,
    val TIPO:String,
    val CODIGO_REFERENCIA:String
)

data class ListaConfirmacionPickingEnvio(
    val picking_items_confirm: Picking_Items_confirm_lista
)

data class Picking_Items_confirm_lista(
    val STATUS: Int,
    val MESSAGE: String
)

data class ListaPickingRecolecta(
    val WORK: String,
    val ITEMS: Int
)

data class ListaPickingRecolecta_valid(
    val resultado: String
)

data class ListaItemsPickingRecolecta(
    val CODIGO: String,
    val DESCRIPCION: String,
    val ALMACEN_ID: Int,
    val AREA_ID: Int,
    val UBICACION: String,
    val BOX: String,
    val UNIDADES: Int,
    val UNIDADES_CONFIRMADAS: Int,
    val PAQUETE: String,
    val UBICACION_ORIGINAL: String,
    val BOX_ORIGINAL: String,
    val ITEM: Int,
    val ECOMMERCE: String,
    val SHIPPING_STATUS: String,
    val FECHA: String,
    val INDEX: Int,
    val TIPO: String,
    val CODIGO_REFERENCIA:String
)


data class ListRecolectaPacking(
    val ITEM_ID:String,
    val TITLE:String,
    val SKU:String,
    val QUANTITY:Int,
    val ID: String,
    val SHIPPMENT_ID: String,
    val DESTINATARIO:String,
    val PACK_ID:String,
    val ECOMMERCE:String,
    val CODIGO: String
)

data class ListProducto(
    val CODIGO: String,
    val DESCRIPCION: String,
    val STOCK_CEDIS: Int,
    val FISICO_DISPONIBLE: Int,
    val APARTADO_PICKING: Int,
    val PISO_PICKING: Int,
    val STOCK_CEDIS_UBICACIONES: String,
    val LINEA_ID: Int,
    val LINEA_NOMBRE: String,
    val LINEA_CODIGO: String,
    val SUBLINEA2_ID: Int,
    val SUBLINEA2_NOMBRE: String,
    val PRC_1: Int,
    val PRC_2: Int,
    val PRC_3: Int,
    val PRECIO_1: Double,
    val PRECIO_2: Double,
    val PRECIO_3: Double,
    val MAYOR_A2: Int,
    val MAYOR_A3: Int,
    val COSTO_ULTIMO: Double,
    val PRECIO_ML: Double,
    val PRECIO_SHOPEE: Double,
    val PRECIO_AMAZON: Double,
    val DESCRIPCION_CHINA: String,
    val COLOR_MEDIDA_CHINA: String,
    val THUMBNAIL: String,
    val CODIGO_SAT: String,
    val DESCRIPCION_SAT: String,
    val UNIDADES_POR_CAJA: Int,
    val GRUPO: String,
    val ID: String,
    val VARIATION_ID: String
)

data class ListaProductoUbicacion(
    val BOX:String,
    val CODIGO:String,
    val FISICO_DISPONIBLE:Int,
    val UBICACION:String
)

data class ListaArea(
    val CANTIDAD_DISPONIBLE:Int,
    val CODIGO:String,
    val DESCRIPCION:String,
    val TOTAL_PIEZAS:Int,
    val UBICACION:String
)


data class ListConsumaInterno(
    val UNIDADES:Int
)

data class listFoliosVentaInterna(
    val FOLIO:Int
)

data class listDatosFolios(
    val CODIGO: String,
    val DESCRIPCION: String,
    val CANTIDAD: Int,
    val UBICACION: String,
    val BOX: String,
    val ITEM: Int,
    val CODIGO_REFERENCIA:String
)

data class listaUnidades(
    val UNIDADES:Int
)


data class ListImagen(
    val CODIGO: String?,
    val DESCRIPCION: String?,
    val URL: String?,
    val INDEX: Float?
)

data class ListImpresoras(
    val ID: Int,
    val IP: String,
    val AREA: String,
    val STATUS: Int
)


data class ListReciboAbastecimiento(
    val FOLIO:String,
    val TOTAL_UNIDADES:Int,
    val CAJA:Int
)

data class ListReciboAbastecimientoItems(
    val ID:Int,
    val ITEM:Int,
    val CANTIDAD:Int,
    val CODIGO:String,
    val DESCRIPCION:String,
    val CANTIDAD_RECIBIDA:Int,
    val POR_RECIBIR:Int,
    val SUBLINEA2_ID:Int,
    val CAJA:String,
    val STATUS:String,
    val REFERENCIA:String,
    val CALIDAD:String,
    val RAZON_CALIDAD:String
)

data class ListItemsAcomodo(
    val UBICACION_ORIGEN: String,
    val BOX_ORIGEN: String,
    val UBICACION_DESTINO: String,
    val BOX_DESTINO: String,
    val CANTIDAD: Int,
    val CODIGO: String,
    val DESCRIPCION: String,
    val ITEM: Int,
    val AREA_DESTINO: Int,
    val CANTIDAD_CONFIRMADA: Int,
    val AREA_DESTINO_NOMBRE: String
)

data class defectos(
    val DEFECTO_GENERAL: String,
    val PREGUNTA: String
)

data class acomodos_con(
    val acomodo_items_ubicacion: acomodo_mensaje_con
)

data class acomodo_mensaje_con(
    val STATUS:Int,
    val MESSAGE: String
)

data class productosPacks(
    val CODIGO: String,
    val DESCRIPCION:String,
    val CANTIDAD:Int
)

data class validarUnidades(
    val UNIDADES:Int
)

data class sugerenciaUbicacion(
    val UBICACION:String,
    val BOX:String,
    val FISICO_DISPONIBLE:Int
)

data class ReubicacionConfir(
    val status: String,
    val message:String
)

data class reubicacion_items_ubicacion(
    val STATUS:Int,
    val MESSAGE:String
)

data class ListaAjusteConfir(
    val message:String
)

data class ListaTareasReacomodo(
    val data: ListaTareasReacomodo_buscarfolios,
    val status: Int,
    val message: String
)

data class ListaTareasReacomodo_buscarfolios(
    val ID: String
)

data class ListExistenciaReacomodo(
    val MENSAJE:String
)
data class ListaDataCheckReacomodo(
    val data:ListaCheckReacomodo
)

data class ListaCheckReacomodo(
    val CODIGO:String,
    val CANTIDAD_CHECK:Int
)

data class ListaReacomodoList(
    val UBICACION_ORIGEN: String,
    val BOX_ORIGEN: String,
    val UBICACION_DESTINO: String,
    val BOX_DESTINO: String,
    val CANTIDAD: Int,
    val CODIGO: String,
    val DESCRIPCION: String,
    val ITEM: Int
)
data class ResultadoLista(
    val data: List<ListaCheckReacomodo>,
    val message: String,
    val status: Int
)
data class ListReacomodoTareasConfirmacion(
    val data:String,
    val status:Int,
    val mensaje:String
)

data class ListaMuestroItems(
    val ID: String,
    val ITEM:Int,
    val UBICACION: String,
    val BOX: String,
    val CODIGO: String,
    val DESCRIPCION: String,
    val STOCK_VIRTUAL:Int,
    val STOCK_CONFIRMADO:Int
)

data class lista_respuestas(
    val status:String,
    val message: String
)

data class lista_productos_valid(
    val CODIGO:String,
    val DESCRIPCION:String
)

data class listaDatosRespuesta(
    val data:listaRespuestaData
)

data class listaRespuestaData(
    val MENSAJE:String
)
data class listaRespuestaDataReacomodo(
    val message:String
)

data class listaDatosRespuesta_faltantes(
    val data:listaRespuestaData_faltantes
)

data class listaRespuestaData_faltantes(
    val CODIGO:String,
    val CANTIDAD:Int,
    val DESCRIPCION:String
)

data class listaResultadoDevolucion(
    val code: String,
    val mensaje:String
)

data class listaResultadoEmbarqueCajas(
    val CANTIDAD_EMPACADA: Int,
    val ID: String
)

data class listaEccommerce(
    val CANAL_ECCOMERCE:String,
    val ID:Int,
    val NOMBRE:String,
    val PREFIJO:String,
    val SELLER_ID:Int
)

data class listaEnbarquePuntoVenta(
    val CANTIDAD_CONFIRMADA: Int,
    val CODIGO:String,
    val DESCRIPCION: String,
    val FOLIO:Int,
    val NAME_CLIENT:String
)

data class ListaMuestroItemsStock(
    val ID: String,
    val ITEM:Int,
    val UBICACION: String,
    val BOX: String,
    val CODIGO: String,
    val DESCRIPCION: String,
    val STOCK_VIRTUAL:Int,
    val STOCK_CONFIRMADO:Int,
    val STOCK_APARTADO:Int,
    val RESERVA_COMBOS_PACKS:Int
)
data class MuestreoUbicaciones_mostrar(
    val CODIGO:String,
    val DESCRIPCION:String,
    val FISICO_DISPONIBLE:Int,
    val BOX:String,
    val UBICACION:String
)


data class ListaItemsCalidad(
    val CODIGO:String,
    val DESCRIPCION:String,
    val CANTIDAD_RECIBIDA:Int,
    val POR_CONFIRMAR:Int,
    val ITEM:Int,
    val ID:Int,
    val PRIORIDAD:Int
)

data class ListaItemsCalidadDevolucion(
    val CODIGO:String,
    val DESCRIPCION:String,
    val CANTIDAD_RECIBIDA:Int,
    val POR_CONFIRMAR:Int,
    val ITEM:Int,
    val ID:Int,
    val OBSERVACION:String,
    val REFERENCIA: String
)

data class ListaItemsEnvioAsig(
    val CODIGO:String,
    val UNIDADES_CONFIRMADAS:Int,
    val CODIGO_REFERENCIA:String,
    val INVENTORY_ID: String,
    val TITLE:String,
    val SKU:String,
    val ENVIO_ID:String,
    val COLOR:String,
    val DESCRIPCION: String
)

data class ListaCajasAsig(
    val CAJA:String,
    val CANTIDAD_EMPACADA:Int
)

data class ListaPaqueteria(
    val PAQUETERIA:String
)

data class ListaDevPr(
    val CANTIDAD:Int,
    val CODIGO:String,
    val DESCRIPCION:String
)

data class listFoliosDevEmbarque(
    val FOLIO:String
)

data class listEccomerce(
    val CANAL_ECCOMERCE:String,
    val ID: Int,
    val NOMBRE:String
)

data class listaPaqueterias(
    val NOMBRE: String
)

data class listaRecomendaciones(
    val CATEGORIA:String,
    val ID: String,
    val MARCA:String,
    val MODELO:String,
    val RECOMENDACIONES:String,
    val RECOMENDACIONES_USO_TIPO:String,
    val TIPO:String,
    val TITLE:String
)

data class listaDevolucionProveedor(
    val FOLIO_COMPRA:String,
    val FOLIO_DEVOLUCION:String,
    val ID:Int,
    val ITEMS:List<listaDevolucionItems>,
    val PROVEEDOR:String
)

data class listaDevolucionItems(
    val CANTIDAD_DEVUELTA:Int,
    val CODIGO:String,
    val DESCRIPCION:String,
    val ITEM:Int
)

data class listaEtiquetaPackingMeli(
    val CODIGO:String,
    val UNIDADES_CONFIRMADAS:Int,
    val CODIGO_REFERENCIA:String,
    val INVENTORY_ID:String,
    val TITLE:String,
    val SKU:String,
    val ENVIO_ID:String,
    val COLOR:String,
    val DESCRIPCION:String
)


data class listaAlmacenDatos(
    val ID:String,
    val NOMBRE:String,
    val PUERTO:String
)