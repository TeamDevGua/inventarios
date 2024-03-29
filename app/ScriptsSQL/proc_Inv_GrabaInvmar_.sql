USE [TCADBGUA]
GO
/****** Object:  StoredProcedure [dbo].[proc_Inv_GrabaInvmar]    Script Date: 02/01/2021 01:32:29 p. m. ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
ALTER PROCEDURE [dbo].[proc_Inv_GrabaInvmar]
@pzacaja varchar(10),
@codinvGinvmar varchar(6), -- codigo inventario '004432'
@zonaGinvmar varchar(3), -- zona 'Z01'
@almGinvmar varchar(4), -- almacen 'GUA1'
@artGinvmar varchar(5), -- articulo '38327'
@cantidad varchar(100), -- cantidad capturada
@usrGinvmar varchar(10), -- clave corta usuario
@areaGinvmar varchar(100), -- area
@empaqGinvmar varchar(10), -- empaque
@tipoUsuario varchar(10)
AS
BEGIN

DECLARE @usuarioActivo VARCHAR(10);

IF @tipoUsuario = 'Auditor'
BEGIN
	SET @usuarioActivo = (SELECT TOP(1) audito FROM tblinventario_tic WHERE ninventario = @codinvGinvmar AND area = @areaGinvmar);
END
	ELSE
BEGIN
	SET @usuarioActivo = (SELECT TOP(1) capturo FROM tblinventario_tic WHERE ninventario = @codinvGinvmar AND area = @areaGinvmar);
END

IF @usuarioActivo = @usrGinvmar --12B
BEGIN
	IF @pzacaja = 'pieza'
	BEGIN
	--Inserta en la tabla invmar la captura
		   INSERT INTO invmar 
		   (ibuff, codigo, cia,alm, salm, art, nor_adi, existencia, marbete, status, verif, exi_otros, fec_conto,usr_conto,Area, exi_cjas,fac_cjas,exi_pzas,fac_pzas)
		   VALUES ('RTF',@codinvGinvmar,'GUA',@zonaGinvmar,@almGinvmar,@artGinvmar,'N',@cantidad,RIGHT('00000' + CAST(CONVERT(INT, (SELECT MAX(marbete) FROM invmar)+1) AS VARCHAR(6)), 6),'1','N','0',Convert(char(8),GETDATE(),112),@usrGinvmar,@areaGinvmar,'0',@empaqGinvmar,@cantidad,'1');
	END
		ELSE
	IF @pzacaja = 'caja'
	BEGIN
	--Inserta en la tabla invmar la captura en cajas
		   INSERT INTO invmar 
		   (ibuff, codigo, cia,alm, salm, art, nor_adi, existencia, marbete, status, verif, exi_otros, fec_conto,usr_conto,Area, exi_cjas,fac_cjas,exi_pzas,fac_pzas)
		   VALUES ('RTF',@codinvGinvmar,'GUA',@zonaGinvmar,@almGinvmar,@artGinvmar,'N',@cantidad,RIGHT('00000' + CAST(CONVERT(INT, (SELECT MAX(marbete) FROM invmar)+1) AS VARCHAR(6)), 6),'1','N','0',Convert(char(8),GETDATE(),112),@usrGinvmar,@areaGinvmar,@cantidad,@empaqGinvmar,'0','1');
	END
	SELECT MAX(marbete) AS [Marbete] FROM invmar WHERE codigo = @codinvGinvmar AND area = @areaGinvmar
END
	ELSE
BEGIN
	SELECT 'ERROR' AS [Marbete]
END


END



