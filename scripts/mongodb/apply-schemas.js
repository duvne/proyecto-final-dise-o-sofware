const collectionSchemas = [
	{
		database: "volt_reservas",
		name: "reservas",
		validator: {
			$jsonSchema: {
				bsonType: "object",
				required: ["_id"],
				properties: {
					_id: { bsonType: "string" },
					id_arrendatario: { bsonType: "string" },
					id_patineta: { bsonType: "string" },
					id_cupon: { bsonType: "string" },
					fecha_inicio: { bsonType: "string" },
					fecha_fin: { bsonType: "string" },
					subtotal_alquiler: { bsonType: "number" },
					descuento_aplicado: { bsonType: "number" },
					total_alquiler: { bsonType: "number" },
					estado: { bsonType: "string" },
					pago: {
						bsonType: "object",
						properties: {
							referencia_pasarela: { bsonType: "string" },
							monto_total: { bsonType: "number" },
							comision_plataforma: { bsonType: "number" },
							estado: { bsonType: "string" },
							fecha_pago: { bsonType: "string" }
						}
					},
					deposito: {
						bsonType: "object",
						properties: {
							monto_retenido: { bsonType: "number" },
							monto_devuelto: { bsonType: "number" },
							estado: { bsonType: "string" },
							fecha_resolucion: { bsonType: "string" }
						}
					},
					entregas_devoluciones: {
						bsonType: "array",
						items: {
							bsonType: "object",
							properties: {
								tipo: { bsonType: "string" },
								fecha_hora: { bsonType: "string" },
								porcentaje_bateria: { bsonType: "number" },
								odometro_km: { bsonType: "number" },
								fotos_url: { bsonType: "string" },
								acta_firmada: { bsonType: "bool" },
								recargo_retraso: { bsonType: "number" }
							}
						}
					},
					liquidacion: {
						bsonType: "object",
						properties: {
							id_propietario: { bsonType: "string" },
							monto_bruto: { bsonType: "number" },
							comision_descontada: { bsonType: "number" },
							monto_neto_propietario: { bsonType: "number" },
							estado: { bsonType: "string" },
							fecha_desembolso: { bsonType: "string" }
						}
					},
					incidencias: {
						bsonType: "array",
						items: {
							bsonType: "object",
							properties: {
								id_reportante: { bsonType: "string" },
								tipo: { bsonType: "string" },
								descripcion: { bsonType: "string" },
								evidencia_url: { bsonType: "string" },
								estado: { bsonType: "string" },
								fecha_resolucion: { bsonType: "string" }
							}
						}
					}
				}
			}
		}
	},
	{
		database: "volt_usuarios",
		name: "usuarios",
		validator: {
			$jsonSchema: {
				bsonType: "object",
				required: ["_id"],
				properties: {
					_id: { bsonType: "string" },
					nombre: { bsonType: "string" },
					documento: { bsonType: "string" },
					correo: { bsonType: "string" },
					password_hash: { bsonType: "string" },
					telefono: { bsonType: "string" },
					fecha_nacimiento: { bsonType: "string" },
					verificado: { bsonType: "bool" },
					estado_cuenta: { bsonType: "string" },
					fecha_registro: { bsonType: "string" },
					roles: {
						bsonType: "array",
						items: { bsonType: "string" }
					},
					perfil_propietario: {
						bsonType: "object",
						properties: {
							cuenta_desembolso: { bsonType: "string" },
							banco_destino: { bsonType: "string" },
							total_patinetas: { bsonType: "number" }
						}
					},
					perfil_arrendatario: {
						bsonType: "object",
						properties: {
							medio_pago_token: { bsonType: "string" },
							total_alquileres: { bsonType: "number" }
						}
					},
					perfil_administrador: { bsonType: ["null"] }
				}
			}
		}
	},
	{
		database: "volt_reservas",
		name: "cupones",
		validator: {
			$jsonSchema: {
				bsonType: "object",
				required: ["_id"],
				properties: {
					_id: { bsonType: "string" },
					codigo: { bsonType: "string" },
					porcentaje_descuento: { bsonType: "number" },
					tope_descuento: { bsonType: "number" },
					fecha_vencimiento: { bsonType: "string" },
					activo: { bsonType: "bool" }
				}
			}
		}
	},
	{
		database: "volt_catalogo",
		name: "patinetas",
		validator: {
			$jsonSchema: {
				bsonType: "object",
				required: ["_id"],
				properties: {
					_id: { bsonType: "string" },
					id_propietario: { bsonType: "string" },
					categoria: { bsonType: "string" },
					marca: { bsonType: "string" },
					modelo: { bsonType: "string" },
					autonomia_km: { bsonType: "number" },
					velocidad_max: { bsonType: "number" },
					estado_fisico: { bsonType: "string" },
					estado_publicacion: { bsonType: "string" },
					activa: { bsonType: "bool" },
					tarifa: {
						bsonType: "object",
						properties: {
							valor_hora: { bsonType: "number" },
							valor_dia: { bsonType: "number" },
							valor_deposito: { bsonType: "number" }
						}
					},
					punto_entrega: {
						bsonType: "object",
						properties: {
							id_punto: { bsonType: "number" },
							direccion: { bsonType: "string" },
							latitud: { bsonType: "number" },
							longitud: { bsonType: "number" },
							verificado: { bsonType: "bool" },
							zona: {
								bsonType: "object",
								properties: {
									id_zona: { bsonType: "number" },
									nombre: { bsonType: "string" },
									ciudad: { bsonType: "string" }
								}
							}
						}
					}
				}
			}
		}
	}
];

for (const schema of collectionSchemas) {
	const targetDb = db.getSiblingDB(schema.database);
	const exists = targetDb.getCollectionInfos({ name: schema.name }).length > 0;
	const options = {
		validator: schema.validator,
		validationLevel: "strict",
		validationAction: "error"
	};

	if (exists) {
		targetDb.runCommand({ collMod: schema.name, ...options });
	} else {
		targetDb.createCollection(schema.name, options);
	}
}

db.getSiblingDB("volt_usuarios").usuarios.createIndex(
	{ correo: 1 },
	{ unique: true, partialFilterExpression: { correo: { $type: "string" } } });
db.getSiblingDB("volt_usuarios").usuarios.createIndex(
	{ documento: 1 },
	{ unique: true, partialFilterExpression: { documento: { $type: "string" } } });
db.getSiblingDB("volt_reservas").reservas.createIndex({ id_patineta: 1, estado: 1, fecha_inicio: 1, fecha_fin: 1 });
db.getSiblingDB("volt_reservas").cupones.createIndex(
	{ codigo: 1 },
	{ unique: true, partialFilterExpression: { codigo: { $type: "string" } } });
db.getSiblingDB("volt_catalogo").patinetas.createIndex({ activa: 1, categoria: 1 });
db.getSiblingDB("volt_catalogo").patinetas.createIndex({ id_propietario: 1 });