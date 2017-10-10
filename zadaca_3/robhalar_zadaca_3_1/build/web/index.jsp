<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Unos IoT uredjaja</title>
    </head>
    <body>
        <br/><br/>
        <div class="container col-sm-12">
            <form action="${pageContext.servletContext.contextPath}/DodajUredjaj" method="post"
                  class="form-horizontal" style="margin-left: -100px;">
                
                <div class="form-group">
                    <label for="naziv" class="control-label col-sm-2">Naziv i adresa: </label>
                    <div class="col-sm-4">
                        <input id="naziv" name="naziv" value="${requestScope.naziv}" placeholder="naziv" class="form-control"/>
                    </div>
                    <div class="col-sm-4">
                        <input id="adresa" name="adresa" value="${requestScope.adresa}" placeholder="adresa" class="form-control"/>
                    </div>
                    <div class="col-sm-2">
                        <input type="submit" name="odabir" value="Geo lokacija" class="btn btn-default"/>
                    </div>
                </div>
                <div class="form-group">
                    <label for="lokacija"  class="control-label col-sm-2">Geo lokacija: </label>
                    <div class="col-sm-8">
                        <input id="lokacija" name="lokacija" value="${requestScope.lokacija}" class="form-control" readonly>
                    </div>
                    <div class="col-sm-2">
                        <input type="submit" name="odabir" value="Spremi" 
                               <c:if test="${(requestScope.adresa==null) || (requestScope.lokacija==null)}">disabled='disabled'</c:if> 
                               class="btn btn-default <c:if test="${(requestScope.adresa==null) || (requestScope.lokacija==null)}">disabled</c:if>"/>
                    </div>
                </div>
                <div class="form-group">
                    <div class="col-sm-2 pull-right">
                        <input type="submit" name="odabir" value="Meteo podaci" 
                           <c:if test="${requestScope.lokacija==null}">disabled='disabled'</c:if> 
                           class="btn btn-default <c:if test="${requestScope.lokacija==null}">disabled</c:if>"/>
                    </div>
                </div>
            </form>
        
            <c:if test="${requestScope.meteo==true}">
                Temp: ${requestScope.temperatura}<br/>
                Vlaga: ${requestScope.vlaga}<br/>
                Tlak: ${requestScope.tlak}<br/>
            </c:if>
            
            <c:if test="${requestScope.successMessage!=null}">
                <br/>
                <div class="alert alert-success">
                    <strong>Success!</strong> ${requestScope.successMessage}
                </div>
            </c:if>
                
            <c:if test="${requestScope.errorMessage!=null}">
                <br/>
                <div class="alert alert-warning">
                    <strong>Error!</strong> ${requestScope.errorMessage}
                </div>
            </c:if>
        </div>
    </body>
</html>
