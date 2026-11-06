package com.example.mastermartini.data

// DATOS FIJOS DE LA EMPRESA
// "object" = existe UNA sola copia en toda la app (no se crea con "DatosEmpresa()").
// Se usa directo: DatosEmpresa.URL_CATALOGO.
// "const val" = constante que se conoce al compilar y nunca cambia.
// Se escribe aquí (y no en cada pantalla) para que, si cambia la dirección,
// se corrija en un solo lugar.
object DatosEmpresa {

    // Catálogo público de Panadería & Pastelería de Master Martini Chile,
    // publicado en su sitio web (archivo PDF). Se abre en el navegador del celular.
    const val URL_CATALOGO =
        "https://mastermartini.cl/wp-content/uploads/2023/07/Panaderia-Pasteleria-Catalogo.pdf"
}
