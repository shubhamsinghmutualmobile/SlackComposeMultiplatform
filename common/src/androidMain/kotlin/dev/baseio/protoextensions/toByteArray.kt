package dev.baseio.protoextensions

import com.google.protobuf.Parser
import dev.baseio.slackdata.securepush.KMWrappedWebPushPublicKey
import dev.baseio.slackdata.protos.SKByteArrayElement
import dev.baseio.slackdata.protos.kmSKByteArrayElement
import dev.baseio.slackdata.securepush.KMHybridRsaCiphertext
import dev.baseio.slackdata.securepush.KMSecureNotification
import dev.baseio.slackdata.securepush.KMWrappedRsaEcdsaPublicKey
import dev.baseio.slackdata.securepush.KMWrappedWebPushPrivateKey
import dev.baseio.slackdata.securepush.WrappedWebPushPublicKey
import dev.baseio.slackdata.securepush.WrappedWebPushPrivateKey
import dev.baseio.slackdata.securepush.kmWrappedWebPushPrivateKey

actual fun KMWrappedWebPushPublicKey.toByteArray(): ByteArray {
    return WrappedWebPushPublicKey.newBuilder()
        .addAllAuthsecret(this.authsecretList.map {
            SKByteArrayElement.newBuilder()
                .setByte(it.byte).build()
        })
        .addAllKeybytes(this.keybytesList.map {
            SKByteArrayElement.newBuilder()
                .setByte(it.byte).build()
        })
        .build().toByteArray()
}

actual fun KMWrappedWebPushPrivateKey.toByteArray(): ByteArray {
    return dev.baseio.slackdata.securepush.WrappedWebPushPrivateKey.newBuilder()
        .addAllAuthsecret(this.authsecretList.map {
            dev.baseio.slackdata.protos.SKByteArrayElement.newBuilder()
                .setByte(it.byte).build()
        })
        .addAllPrivatekeybytes(this.privatekeybytesList.map {
            dev.baseio.slackdata.protos.SKByteArrayElement.newBuilder()
                .setByte(it.byte).build()
        }) .addAllPublickeybytes(this.publickeybytesList.map {
            dev.baseio.slackdata.protos.SKByteArrayElement.newBuilder()
                .setByte(it.byte).build()
        })
        .build().toByteArray()
}

actual fun KMHybridRsaCiphertext.toByteArray(): ByteArray {
    return dev.baseio.slackdata.securepush.HybridRsaCiphertext.newBuilder()
        .addAllSymmetrickeyciphertext(this.symmetrickeyciphertextList.map {
            dev.baseio.slackdata.protos.SKByteArrayElement.newBuilder()
                .setByte(it.byte).build()
        })
        .addAllPayloadciphertext(this.payloadciphertextList.map {
            dev.baseio.slackdata.protos.SKByteArrayElement.newBuilder()
                .setByte(it.byte).build()
        })
        .build().toByteArray()
}

actual fun KMWrappedRsaEcdsaPublicKey.toByteArray(): ByteArray {
    return dev.baseio.slackdata.securepush.WrappedRsaEcdsaPublicKey.newBuilder()
        .addAllKeybytes(this.keybytesList.map {
            dev.baseio.slackdata.protos.SKByteArrayElement.newBuilder()
                .setByte(it.byte).build()
        })
        .build().toByteArray()
}

actual fun KMSecureNotification.toByteArray(): ByteArray{
    return dev.baseio.slackdata.securepush.SecureNotification.newBuilder()
        .setId(this.id)
        .setTitle(this.title)
        .setBody(this.body)
        .build()
        .toByteArray()
}

actual fun ByteArray.toKMWrappedWebPushPrivateKey(): KMWrappedWebPushPrivateKey {
    val privateKey = WrappedWebPushPrivateKey.parseFrom(this)
    return kmWrappedWebPushPrivateKey {
        this.authsecretList.addAll(privateKey.authsecretList.map {it->
            kmSKByteArrayElement {
                this.byte = it.byte
            }
        })
        this.privatekeybytesList.addAll(privateKey.privatekeybytesList.map { it->
            kmSKByteArrayElement {
                this.byte = it.byte
            }
        })
        this.publickeybytesList.addAll(privateKey.publickeybytesList.map {it->
            kmSKByteArrayElement {
                this.byte = it.byte
            }
        })
    }
}