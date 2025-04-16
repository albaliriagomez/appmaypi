package com.torrezpillcokevin.nuna.models

import android.os.Parcel
import android.os.Parcelable

data class Persona(
    val nombre: String,
    val apellido: String,
    val edad: Int,
    val genero: String,
    val descripcion: String,
    val fechaNacimiento: String,
    val fechaDesaparicion: String,
    val lugarDesaparicion: String,
    val estadoInvestigacion: String,
    val imagen: Int,
    val caracteristicas: String,
    val tiempoDesaparecido: String,
    val nombreContacto: String,
    val telefonoContacto: String,
    val emailContacto: String,
    val ubicacionDesaparicion: String = "" // Campo adicional para coordenadas (valor por defecto vacío)
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",  // nombreContacto
        parcel.readString() ?: "",  // telefonoContacto
        parcel.readString() ?: "",  // emailContacto
        parcel.readString() ?: ""   // ubicacionDesaparicion
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(nombre)
        parcel.writeString(apellido)
        parcel.writeInt(edad)
        parcel.writeString(genero)
        parcel.writeString(descripcion)
        parcel.writeString(fechaNacimiento)
        parcel.writeString(fechaDesaparicion)
        parcel.writeString(lugarDesaparicion)
        parcel.writeString(estadoInvestigacion)
        parcel.writeInt(imagen)
        parcel.writeString(caracteristicas)
        parcel.writeString(tiempoDesaparecido)
        parcel.writeString(nombreContacto)      // Añadido
        parcel.writeString(telefonoContacto)    // Añadido
        parcel.writeString(emailContacto)       // Añadido
        parcel.writeString(ubicacionDesaparicion) // Añadido
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Persona> {
        override fun createFromParcel(parcel: Parcel): Persona {
            return Persona(parcel)
        }

        override fun newArray(size: Int): Array<Persona?> {
            return arrayOfNulls(size)
        }
    }
}