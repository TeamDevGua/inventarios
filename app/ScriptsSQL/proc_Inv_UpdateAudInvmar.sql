USE [TCADBGUA]
GO
/****** Object:  StoredProcedure [dbo].[proc_Inv_UpdateAudInvmar]    Script Date: 16/01/2021 09:46:01 a. m. ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- =============================================
-- Author:		<Daniel Ponce>
-- Create date: <11/01/2021>
-- Description:	<Update a la cantidad en la tabla invmar>
-- =============================================
ALTER PROCEDURE [dbo].[proc_Inv_UpdateAudInvmar] 
@Tipo varchar(10), 
@Cantidad varchar(100), 
@Marbete varchar(100),
@Codigo varchar(100),
@Art varchar(100),
@Area varchar(100),
@Zona varchar(3),
@Subalm varchar(100)
AS
BEGIN
	IF @Tipo = 'CAJA'
	BEGIN
		--Obtiene factor de empaque de cajas
		DECLARE @fac_emp FLOAT
		SET @fac_emp = (SELECT invars.fac_ent_sal*invars.fac_minimo FROM invars WHERE cve_art = @Art AND alm = @Zona AND sub_alm = @Subalm);
		--Realiza la multiplicacion para obtener el numero total de piezas (cant cajas * pzas caja)
		DECLARE @cant FLOAT
		SET @cant = @fac_emp * convert(float,@Cantidad)
		--Realiza el update a las cajas
		UPDATE invmar SET existencia = @cant , exi_cjas = @Cantidad WHERE ibuff = 'RTF' AND marbete = @Marbete AND codigo = @Codigo AND art = @Art  AND Area = @Area
	END
		ELSE
	BEGIN --PIEZA
		--No requiere realizar calculo
		UPDATE invmar SET existencia = @Cantidad , exi_pzas = @Cantidad WHERE ibuff = 'RTF' AND marbete= @Marbete AND codigo = @Codigo AND art = @Art	AND Area = @Area	--mandar subalm
	END
END
